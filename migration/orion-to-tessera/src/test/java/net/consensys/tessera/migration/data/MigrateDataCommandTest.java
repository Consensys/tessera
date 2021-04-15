package net.consensys.tessera.migration.data;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import com.quorum.tessera.io.IOCallback;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.consensys.tessera.migration.OrionKeyHelper;
import org.apache.commons.io.FileUtils;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fusesource.leveldbjni.JniDBFactory.factory;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class MigrateDataCommandTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MigrateDataCommandTest.class);

    private MigrateDataCommand migrateDataCommand;

    private InboundDbHelper inboundDbHelper;

    private TesseraJdbcOptions tesseraJdbcOptions;

    private Path orionConfigDir;

    private String tesseraJdbcUrl;

    @Rule
    public TemporaryFolder outputDir = new TemporaryFolder();

    private OrionKeyHelper orionKeyHelper;

    private OrionDbType orionDbType;

    public MigrateDataCommandTest(TestConfig testConfig) {
        this.orionConfigDir = testConfig.getConfigDirPath();
        this.orionDbType = testConfig.getOrionDbType();
    }

    @Before
    public void beforeTest() throws Exception {

        FileUtils.copyDirectory(orionConfigDir.toFile(),outputDir.getRoot());

        Path workfingOrionConfigDir = outputDir.getRoot().toPath();

        tesseraJdbcUrl = "jdbc:h2:" + workfingOrionConfigDir.toAbsolutePath() + "/" + UUID.randomUUID().toString() + ".db";
        final Path orionConfigFile = Paths.get(workfingOrionConfigDir.toString(),"orion.conf");
        assertThat(orionConfigFile).exists();

        Toml toml = new Toml().read(orionConfigFile.toFile());

        Map orionConfig = new HashMap(toml.toMap());
        orionConfig.put("workdir",workfingOrionConfigDir.toString());
        TomlWriter tomlWriter = new TomlWriter();

        Path adjustedOrionConfigFile = workfingOrionConfigDir.resolve("orion-adjusted.conf");
        tomlWriter.write(orionConfig,Files.newOutputStream(adjustedOrionConfigFile));

        inboundDbHelper = mock(InboundDbHelper.class);
        if(orionDbType == OrionDbType.LEVELDB) {
            Options options = new Options();
            //options.logger(s -> System.out.println(s));
            options.createIfMissing(true);
            String dbname = "routerdb";
            final DB leveldb = IOCallback.execute(
                () -> factory.open(workfingOrionConfigDir.resolve(dbname).toAbsolutePath().toFile(), options)
            );
            when(inboundDbHelper.getLevelDb()).thenReturn(Optional.of(leveldb));
            when(inboundDbHelper.getInputType()).thenReturn(InputType.LEVELDB);
        }
        else {
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl("jdbc:postgresql://localhost:5432/orion");
            hikariConfig.setUsername("postgres");
            hikariConfig.setPassword("postgres");

            HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig);
            when(inboundDbHelper.getJdbcDataSource()).thenReturn(Optional.of(hikariDataSource));
            when(inboundDbHelper.getInputType()).thenReturn(InputType.JDBC);

//            try(Connection connection = hikariDataSource.getConnection()) {
//                try(Statement statement = connection.createStatement()) {
//
//                    statement.execute("DROP TABLE IF EXISTS store");
//
//                    statement.execute("CREATE TABLE store (\n" +
//                        "  key char(60),\n" +
//                        "  value bytea,\n" +
//                        "  primary key(key)\n" +
//                        ")");
//                }
//
//                Path sqlFile = workfingOrionConfigDir.resolve("sql").resolve("store.sql");
//
//                try(Statement statement = connection.createStatement()) {
//
//                    Files.lines(sqlFile).forEach(line -> {
//                        try {
//                            statement.addBatch(line);
//                        } catch (SQLException sqlException) {
//                            throw new RuntimeException(sqlException);
//                        }
//                    });
//                    statement.executeBatch();
//                }
//
//                try(ResultSet count = connection.createStatement()
//                    .executeQuery("SELECT COUNT(*) FROM store")) {
//                    assertThat(count.next()).isTrue();
//                    assertThat(count.getLong(1)).isEqualTo(Files.lines(sqlFile).count());
//                }
//            }
        }

        tesseraJdbcOptions = mock(TesseraJdbcOptions.class);
        when(tesseraJdbcOptions.getAction()).thenReturn("drop-and-create");
        when(tesseraJdbcOptions.getUrl()).thenReturn(tesseraJdbcUrl);
        when(tesseraJdbcOptions.getUsername()).thenReturn("junit");
        when(tesseraJdbcOptions.getPassword()).thenReturn("junit");

        orionKeyHelper = OrionKeyHelper.from(adjustedOrionConfigFile);

        migrateDataCommand = new MigrateDataCommand(inboundDbHelper, tesseraJdbcOptions, orionKeyHelper);

        MigrationInfoFactory.create(inboundDbHelper);
    }

    @After
    public void afterTest() {
        MigrationInfo.clear();
    }


    @Test
    public void migrate() throws Exception {


        Map<PayloadType,Long> result = migrateDataCommand.call();
        assertThat(result)
            .containsOnlyKeys(PayloadType.ENCRYPTED_PAYLOAD,PayloadType.PRIVACY_GROUP_PAYLOAD);

        MigrationInfo migrationInfo = MigrationInfo.getInstance();
        assertThat(result.get(PayloadType.ENCRYPTED_PAYLOAD)).isEqualTo(migrationInfo.getTransactionCount());
        assertThat(result.get(PayloadType.PRIVACY_GROUP_PAYLOAD)).isEqualTo(migrationInfo.getPrivacyGroupCount());


    }


    @Parameterized.Parameters(name = "{0}")
    public static List<TestConfig> configs() throws IOException {
        List<TestConfig> levelDbConfigs = Files.list(Paths.get("samples"))
            .filter(Files::isDirectory)
            .flatMap(d -> {
                try {
                    return Files.list(d).filter(Files::isDirectory);
                } catch (IOException ioException) {
                    throw new UncheckedIOException(ioException);
                }
            }).map(p -> new TestConfig(OrionDbType.LEVELDB,p))
            .collect(Collectors.toUnmodifiableList());


        List<TestConfig> postgresConfigs = Files.list(Paths.get("pgsamples"))
            .filter(Files::isDirectory)
            .flatMap(d -> {
                try {
                    return Files.list(d).filter(Files::isDirectory);
                } catch (IOException ioException) {
                    throw new UncheckedIOException(ioException);
                }
            }).map(p -> new TestConfig(OrionDbType.POSTGRES,p))
            .collect(Collectors.toUnmodifiableList());

        return List.copyOf(postgresConfigs);
    }

    static class TestConfig {
        private OrionDbType orionDbType;

        private Path configDirPath;

        TestConfig(OrionDbType orionDbType, Path configDirPath) {
            this.orionDbType = orionDbType;
            this.configDirPath = configDirPath;
        }

        public OrionDbType getOrionDbType() {
            return orionDbType;
        }

        public Path getConfigDirPath() {
            return configDirPath;
        }
    }


    enum OrionDbType {
        LEVELDB,
        POSTGRES,
        ORACLE
    }




}
