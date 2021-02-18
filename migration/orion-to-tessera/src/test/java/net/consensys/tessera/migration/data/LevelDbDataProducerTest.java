package net.consensys.tessera.migration.data;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.quorum.tessera.encryption.EncryptorFactory;
import com.quorum.tessera.io.IOCallback;
import net.consensys.tessera.migration.OrionKeyHelper;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fusesource.leveldbjni.JniDBFactory.factory;

@RunWith(Parameterized.class)
public class LevelDbDataProducerTest {

    private LevelDbDataProducer orionDataAdapter;

    private OrionKeyHelper orionKeyHelper;

    private Disruptor<OrionDataEvent> orionDataEventDisruptor;

    private EncryptedKeyMatcher encryptedKeyMatcher;

    private RecipientBoxHelper recipientBoxHelper;

    private TestInfo testInfo;

    private CountDownLatch eventCounter;

    private EntityManagerFactory entityManagerFactory;

    private Disruptor<TesseraDataEvent> tesseraDataEventDisruptor;

    private CountDownLatch tesseraEventCOunter;

    private MigrationInfo migrationInfo;

    private String tesseraJdbcUrl;

    public LevelDbDataProducerTest(TestInfo testInfo) {
        this.testInfo = testInfo;
    }

    @Before
    public void beforeTest() throws Exception {
        Path pwd = Paths.get("").toAbsolutePath();
        tesseraJdbcUrl = "jdbc:h2:"+ pwd +"/"+ UUID.randomUUID().toString() +".db";

        MigrationInfo.clear();

        Map<String,String> jdbcProperties = new HashMap<>();
        jdbcProperties.put("javax.persistence.jdbc.user", "junit");
        jdbcProperties.put("javax.persistence.jdbc.password", "junit");
        jdbcProperties.put("javax.persistence.jdbc.url", tesseraJdbcUrl);

        jdbcProperties.put("eclipselink.logging.level", "FINE");
        jdbcProperties.put("eclipselink.logging.parameters", "true");
        jdbcProperties.put("eclipselink.logging.level.sql", "FINE");

        jdbcProperties.put("eclipselink.jdbc.batch-writing","JDBC");
        jdbcProperties.put("eclipselink.jdbc.batch-writing.size","100");
        jdbcProperties.put("eclipselink.connection-pool.initial","10");
        jdbcProperties.put("eclipselink.connection-pool.min","10");
        jdbcProperties.put("eclipselink.connection-pool.max","10");

        jdbcProperties.put("javax.persistence.schema-generation.database.action", "create");
        entityManagerFactory = Persistence.createEntityManagerFactory("tessera-em",jdbcProperties);

        eventCounter = new CountDownLatch(testInfo.getRowCount());

        orionDataEventDisruptor = new Disruptor<>(
            OrionDataEvent.FACTORY,
            8,
            (ThreadFactory) Thread::new,
            ProducerType.SINGLE,
            new BlockingWaitStrategy());

        orionKeyHelper = OrionKeyHelper.from(testInfo.getConfigFilePath());
        encryptedKeyMatcher = new EncryptedKeyMatcher(
                                    orionKeyHelper,
                                    new EncryptorHelper(EncryptorFactory.newFactory("NACL").create())
        );
        recipientBoxHelper = new RecipientBoxHelper(orionKeyHelper);

        Options options = new Options();
        //options.logger(s -> System.out.println(s));
        options.createIfMissing(true);
        String dbname = "routerdb";

        Path storageDir = testInfo.getStorageDir();
        final DB leveldb = IOCallback.execute(
            () -> factory.open(storageDir.resolve(dbname).toAbsolutePath().toFile(), options)
        );

        new LeveldbMigrationInfoFactory().init(leveldb);
        migrationInfo = MigrationInfo.getInstance();
        System.out.println("migrationInfo "+ migrationInfo);
        tesseraDataEventDisruptor = new Disruptor<>(
            TesseraDataEvent.FACTORY,
            32,
            r -> {
                return new Thread(r,"TesseraDataEvent");
            },
            ProducerType.MULTI,
            new BlockingWaitStrategy());

        int count = migrationInfo.getTransactionCount() + migrationInfo.getPrivacyGroupCount();
        tesseraEventCOunter = new CountDownLatch(count);
        //entityManager.setFlushMode(FlushModeType.AUTO);

        tesseraDataEventDisruptor
            .handleEventsWith((event, sequence, endOfBatch) -> {
                EntityManager entityManager = entityManagerFactory.createEntityManager();
                EntityTransaction entityTransaction = entityManager.getTransaction();
                entityTransaction.begin();
                entityManager.persist(event.getEntity());
                entityTransaction.commit();
                event.reset();
                tesseraEventCOunter.countDown();
            }
        );

        tesseraDataEventDisruptor.start();


        orionDataEventDisruptor
            .handleEventsWith(new PrivacyGroupLookupHandler(recipientBoxHelper,encryptedKeyMatcher))
            .handleEventsWith(
                new ConvertToTransactionEntity(tesseraDataEventDisruptor),
                new ConvertToPrivacyGroupEntity(tesseraDataEventDisruptor)
            )
            .handleEventsWith((orionEvent, l, b) -> {
                if(orionEvent.getPayloadType() == PayloadType.ENCRYPTED_PAYLOAD) {
                  //  System.out.println(orionEvent);
                }
                orionEvent.reset();
                eventCounter.countDown();
            });
        orionDataAdapter = new LevelDbDataProducer(leveldb, orionDataEventDisruptor);
        orionDataEventDisruptor.start();
    }

    @After
    public void afterTest() {

        orionDataEventDisruptor.shutdown();
        tesseraDataEventDisruptor.shutdown();
        MigrationInfo.clear();
    }

    @Test
    public void doStuff() throws Exception {

        orionDataAdapter.start();

        assertThat(eventCounter.await(2, TimeUnit.MINUTES)).isTrue();
        assertThat(eventCounter.getCount()).isZero();

        assertThat(tesseraEventCOunter.await(3, TimeUnit.MINUTES)).isTrue();
        assertThat(tesseraEventCOunter.getCount()).isZero();

        Map<String,String> jdbcProperties = new HashMap<>();
        jdbcProperties.put("javax.persistence.jdbc.user", "junit");
        jdbcProperties.put("javax.persistence.jdbc.password", "junit");
        jdbcProperties.put("javax.persistence.jdbc.url",tesseraJdbcUrl);
        jdbcProperties.put("eclipselink.logging.logger", "org.eclipse.persistence.logging.slf4j.SLF4JLogger");
        jdbcProperties.put("eclipselink.logging.level", "FINE");
        jdbcProperties.put("eclipselink.logging.parameters", "true");
        jdbcProperties.put("eclipselink.logging.level.sql", "FINE");

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("tessera-em",jdbcProperties);

        EntityManager entityManager = emf.createEntityManager();

        assertThat(entityManager.createNamedQuery("EncryptedTransaction.Upcheck",Long.class).getSingleResult())
            .isEqualTo(migrationInfo.getTransactionCount());

        assertThat(entityManager.createQuery("select count(p) from PrivacyGroupEntity p",Long.class).getSingleResult())
            .isEqualTo(migrationInfo.getPrivacyGroupCount());
    }

    @Parameterized.Parameters(name = "{0}")
    public static List<TestInfo> params() throws URISyntaxException {

        return List.of(
            new TestInfo("Locked passwords 32 records",32, Paths.get(""),Paths.get("orion.conf")),
            new TestInfo("Unlocked passwords 2218 records",2218, Paths.get("samples","10k","orion"),Paths.get("samples","10k","orion","orion.conf")),
            new TestInfo("Unlocked passwords 57057 records",57057, Paths.get("samples","100k","orion"),Paths.get("samples","100k","orion","orion.conf")),
            new TestInfo("Unlocked passwords 12000 records",12000, Paths.get("samples","120k","orion"),Paths.get("samples","120k","orion","orion.conf"))

        );
    }

    static class TestInfo {

        private String description;

        private int rowCount;

        private Path storageDir;

        private Path configFilePath;

        TestInfo(String description,int rowCount,Path storageDir,Path configFilePath) {
            this.storageDir = storageDir;
            this.rowCount = rowCount;
            this.configFilePath = configFilePath;
            this.description = description;
        }

        public Path getStorageDir() {
            return storageDir;
        }

        public int getRowCount() {
            return rowCount;
        }

        public Path getConfigFilePath() {
            return configFilePath;
        }

        @Override
        public String toString() {
            return "TestInfo{" +
                "description='" + description + '\'' +
                ", configFilePath=" + configFilePath +
                '}';
        }
    }
}
