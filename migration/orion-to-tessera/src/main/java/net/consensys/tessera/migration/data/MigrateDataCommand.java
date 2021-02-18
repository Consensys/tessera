package net.consensys.tessera.migration.data;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.quorum.tessera.encryption.Encryptor;
import com.quorum.tessera.encryption.EncryptorFactory;
import net.consensys.tessera.migration.OrionKeyHelper;
import org.iq80.leveldb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public class MigrateDataCommand implements Callable<Boolean> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MigrateDataCommand.class);

    private TesseraJdbcOptions tesseraJdbcOptions;

    private OrionKeyHelper orionKeyHelper;

    private Encryptor tesseraEncryptor = EncryptorFactory.newFactory("NACL").create();

    private InboundDbHelper inboundDbHelper;

    static EntityManagerFactory createEntityManagerFactory(TesseraJdbcOptions jdbcOptions) {
        Map<String,String> jdbcProperties = new HashMap<>();
        jdbcProperties.put("javax.persistence.jdbc.user", jdbcOptions.getUsername());
        jdbcProperties.put("javax.persistence.jdbc.password", jdbcOptions.getPassword());
        jdbcProperties.put("javax.persistence.jdbc.url", jdbcOptions.getUrl());

        jdbcProperties.put("eclipselink.logging.level", "FINE");
        jdbcProperties.put("eclipselink.logging.parameters", "true");
        jdbcProperties.put("eclipselink.logging.level.sql", "FINE");

        jdbcProperties.put("eclipselink.jdbc.batch-writing","JDBC");
        jdbcProperties.put("eclipselink.jdbc.batch-writing.size","100");
        jdbcProperties.put("eclipselink.connection-pool.initial","10");
        jdbcProperties.put("eclipselink.connection-pool.min","10");
        jdbcProperties.put("eclipselink.connection-pool.max","10");

        jdbcProperties.put("javax.persistence.schema-generation.database.action", jdbcOptions.getAction());

        return Persistence.createEntityManagerFactory("tessera-em", jdbcProperties);
    }

    public MigrateDataCommand(
            InboundDbHelper inboundDbHelper,
            TesseraJdbcOptions tesseraJdbcOptions,
            OrionKeyHelper orionKeyHelper) {

        this.inboundDbHelper = inboundDbHelper;
        this.tesseraJdbcOptions = tesseraJdbcOptions;
        this.orionKeyHelper = orionKeyHelper;
    }

    @Override
    public Boolean call() throws Exception {

        final InputType inputType = inboundDbHelper.getInputType();

        switch (inputType) {
            case LEVELDB:
                DB leveldb = inboundDbHelper.getLevelDb().get();
                new LeveldbMigrationInfoFactory().init(leveldb);
                break;

            default: throw new UnsupportedOperationException(inputType +" Is not supported");
        }

        final EntityManagerFactory entityManagerFactory = createEntityManagerFactory(tesseraJdbcOptions);

        final MigrationInfo migrationInfo = MigrationInfo.getInstance();
        final CountDownLatch insertedRowCounter = new CountDownLatch(migrationInfo.getTransactionCount() + migrationInfo.getPrivacyGroupCount());

        Disruptor<TesseraDataEvent> tesseraDataEventDisruptor = new Disruptor<>(
            TesseraDataEvent.FACTORY,
            32,
            r -> {
                return new Thread(r,"TesseraDataEvent");
            },
            ProducerType.MULTI,
            new BlockingWaitStrategy());

        tesseraDataEventDisruptor
            .handleEventsWith((event, sequence, endOfBatch) -> {
                    EntityManager entityManager = entityManagerFactory.createEntityManager();
                    EntityTransaction entityTransaction = entityManager.getTransaction();
                    entityTransaction.begin();
                    entityManager.persist(event.getEntity());
                    entityTransaction.commit();
                    event.reset();
                    insertedRowCounter.countDown();
                }
            );

        Disruptor<OrionDataEvent> orionDataEventDisruptor =
                new Disruptor<>(
                    OrionDataEvent.FACTORY,
                        16,
                    r -> {
                        return new Thread(r,"OrionDataEvent");
                    },
                        ProducerType.SINGLE,
                        new BlockingWaitStrategy());


        final DataProducer dataProducer;
        switch (inputType) {
            case LEVELDB:
                dataProducer =
                    new LevelDbDataProducer(inboundDbHelper.getLevelDb().get(), orionDataEventDisruptor);
                break;
            default:
                throw new UnsupportedOperationException("");
        }

        EncryptorHelper encryptorHelper = new EncryptorHelper(tesseraEncryptor);
        EncryptedKeyMatcher encryptedKeyMatcher = new EncryptedKeyMatcher(orionKeyHelper,encryptorHelper);
        RecipientBoxHelper recipientBoxHelper = new RecipientBoxHelper(orionKeyHelper);

        orionDataEventDisruptor
            .handleEventsWith(new PrivacyGroupLookupHandler(recipientBoxHelper,encryptedKeyMatcher))
            .handleEventsWith(
                new ConvertToPrivacyGroupEntity(tesseraDataEventDisruptor),
                new ConvertToTransactionEntity(tesseraDataEventDisruptor));


        tesseraDataEventDisruptor.start();
        orionDataEventDisruptor.start();
        dataProducer.start();

        insertedRowCounter.await();

        LOGGER.info("DONE");

        orionDataEventDisruptor.shutdown();
        tesseraDataEventDisruptor.shutdown();

        return Boolean.TRUE;
    }
}
