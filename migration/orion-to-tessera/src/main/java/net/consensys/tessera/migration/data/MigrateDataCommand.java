package net.consensys.tessera.migration.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.quorum.tessera.encryption.Encryptor;
import com.quorum.tessera.encryption.EncryptorFactory;
import net.consensys.tessera.migration.OrionKeyHelper;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadFactory;

public class MigrateDataCommand implements Callable<Boolean> {

    private TesseraJdbcOptions tesseraJdbcOptions;

    private OrionKeyHelper orionKeyHelper;

    private Encryptor tesseraEncryptor = EncryptorFactory.newFactory("NACL").create();

    private InboundDbHelper inboundDbHelper;

    private ObjectMapper cborObjectMapper = JacksonObjectMapperFactory.createCborObjectMapper();

    static EntityManagerFactory createEntityManagerFactory(TesseraJdbcOptions jdbcOptions) {
        Map jdbcProperties = new HashMap<>();
        jdbcProperties.put("javax.persistence.jdbc.user", jdbcOptions.getUsername());
        jdbcProperties.put("javax.persistence.jdbc.password", jdbcOptions.getPassword());
        jdbcProperties.put("javax.persistence.jdbc.url", jdbcOptions.getUrl());
        jdbcProperties.put("eclipselink.logging.level", "FINE");
        jdbcProperties.put("eclipselink.logging.parameters", "true");
        jdbcProperties.put("eclipselink.logging.level.sql", "FINE");

        jdbcProperties.put("javax.persistence.schema-generation.database.action", jdbcOptions.getAction());

        return Persistence.createEntityManagerFactory("tessera-em", jdbcProperties);
    }

    public MigrateDataCommand(
            InboundDbHelper inboundDbHelper, TesseraJdbcOptions tesseraJdbcOptions, OrionKeyHelper orionKeyHelper) {
        this.inboundDbHelper = inboundDbHelper;
        this.tesseraJdbcOptions = tesseraJdbcOptions;
        this.orionKeyHelper = orionKeyHelper;
    }

    @Override
    public Boolean call() throws Exception {

        Disruptor<OrionEvent> disruptor =
                new Disruptor<>(
                    OrionEvent.FACTORY,
                        128,
                        (ThreadFactory) Thread::new,
                        ProducerType.SINGLE,
                        new BlockingWaitStrategy());

        InputType inputType = inboundDbHelper.getInputType();
        final OrionDataAdapter inboundAdapter;
        switch (inputType) {
            case LEVELDB:
                inboundAdapter =
                        new LevelDbOrionDataAdapter(inboundDbHelper.getLevelDb().get(), cborObjectMapper, disruptor,orionKeyHelper,tesseraEncryptor);
                break;
            case JDBC:
                DataSource dataSource = inboundDbHelper.getJdbcDataSource().get();
                inboundAdapter = new JdbcOrionDataAdapter(dataSource, cborObjectMapper, disruptor,orionKeyHelper,tesseraEncryptor);
                break;
            default:
                throw new UnsupportedOperationException("");
        }

        EntityManagerFactory entityManagerFactory = createEntityManagerFactory(tesseraJdbcOptions);

        PersistPrivacyGroupEventHandler persistPrivacyGroupEventHandler = new PersistPrivacyGroupEventHandler(entityManagerFactory);
        PersistTransactionEventHandler persistTransactionEventHandler = new PersistTransactionEventHandler(entityManagerFactory);
        CompletionHandler completionHandler = new CompletionHandler();
        disruptor
                .handleEventsWith(
                    persistPrivacyGroupEventHandler,
                    persistTransactionEventHandler)
                    .then(completionHandler);

        disruptor.start();

        inboundAdapter.start();

        completionHandler.await();

        disruptor.shutdown();

        return Boolean.TRUE;
    }
}
