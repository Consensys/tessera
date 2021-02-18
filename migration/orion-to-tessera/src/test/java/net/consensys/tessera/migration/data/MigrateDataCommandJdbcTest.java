package net.consensys.tessera.migration.data;

import com.quorum.tessera.io.IOCallback;
import net.consensys.tessera.migration.OrionKeyHelper;
import org.h2.jdbcx.JdbcDataSource;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fusesource.leveldbjni.JniDBFactory.factory;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class MigrateDataCommandJdbcTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MigrateDataCommandJdbcTest.class);

    private MigrateDataCommand migrateDataCommand;

    private InboundDbHelper inboundDbHelper;

    private TesseraJdbcOptions tesseraJdbcOptions;

    private Path orionConfigDir;

    private String tesseraJdbcUrl;

    private Path pwd = Paths.get("").toAbsolutePath();

    public MigrateDataCommandJdbcTest(Path orionConfigDir) {
        this.orionConfigDir = orionConfigDir;
    }

    @Before
    public void beforeTest() throws Exception {
        tesseraJdbcUrl = "jdbc:h2:" + pwd + "/" + UUID.randomUUID().toString() + ".db";
        final Path orionConfigFile = orionConfigDir.resolve("orion.conf");

        Options options = new Options();
        //options.logger(s -> System.out.println(s));
        options.createIfMissing(true);
        String dbname = "routerdb";
        final DB leveldb = IOCallback.execute(
            () -> factory.open(orionConfigDir.resolve(dbname).toAbsolutePath().toFile(), options)
        );

        JdbcDataSource orionDataSource = new JdbcDataSource();
        orionDataSource.setURL("jdbc:h2:" + pwd + "/orion-" + UUID.randomUUID().toString() + ".db");
        orionDataSource.setUser("orion");
        orionDataSource.setPassword("orion");
        LevelDbToJdbcUtil.copy(leveldb,orionDataSource);

        inboundDbHelper = mock(InboundDbHelper.class);
        when(inboundDbHelper.getJdbcDataSource()).thenReturn(Optional.of(orionDataSource));
        when(inboundDbHelper.getInputType()).thenReturn(InputType.JDBC);

        tesseraJdbcOptions = mock(TesseraJdbcOptions.class);
        when(tesseraJdbcOptions.getAction()).thenReturn("drop-and-create");
        when(tesseraJdbcOptions.getUrl()).thenReturn(tesseraJdbcUrl);
        when(tesseraJdbcOptions.getUsername()).thenReturn("junit");
        when(tesseraJdbcOptions.getPassword()).thenReturn("junit");

        OrionKeyHelper orionKeyHelper = OrionKeyHelper.from(orionConfigFile);

        migrateDataCommand = new MigrateDataCommand(inboundDbHelper, tesseraJdbcOptions, orionKeyHelper);

    }

    @After
    public void afterTest() {
        MigrationInfo.clear();
    }


    @Test
    public void migrate() throws Exception {
        assertThat(migrateDataCommand.call()).isTrue();

        MigrationInfo migrationInfo = MigrationInfo.getInstance();
        LOGGER.info(" HERE {}", migrationInfo);

        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL(tesseraJdbcUrl);
        dataSource.setUser("junit");
        dataSource.setPassword("junit");


        try (
            Connection connection = dataSource.getConnection();
            ResultSet txnRs = connection.createStatement().executeQuery("SELECT COUNT(*) FROM ENCRYPTED_TRANSACTION");
            ResultSet privacyGroupRs = connection.createStatement().executeQuery("SELECT COUNT(*) FROM PRIVACY_GROUP")
        ) {

            assertThat(txnRs.next()).isTrue();
            assertThat(txnRs.getLong(1)).isEqualTo(migrationInfo.getTransactionCount());

            assertThat(privacyGroupRs.next()).isTrue();
            assertThat(privacyGroupRs.getLong(1)).isEqualTo(migrationInfo.getPrivacyGroupCount());
        }
    }


    @Parameterized.Parameters(name = "{0}")
    public static List<Path> configs() {
        return List.of(
            Paths.get("samples", "10k", "orion"),
            Paths.get("samples","100k","orion"),
            Paths.get("samples","120k","orion")
        );
    }




}
