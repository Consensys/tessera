package net.consensys.tessera.migration.data;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import com.quorum.tessera.config.*;
import com.quorum.tessera.enclave.*;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.io.IOCallback;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.consensys.tessera.migration.MigrateCommandTest;
import net.consensys.tessera.migration.OrionKeyHelper;
import org.apache.commons.io.FileUtils;
import org.apache.tuweni.crypto.sodium.Box;
import org.h2.jdbcx.JdbcDataSource;
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
import java.sql.*;
import java.util.*;
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

    private String tesseraJdbcUrl;

    @Rule
    public TemporaryFolder outputDir = new TemporaryFolder();

    private OrionKeyHelper orionKeyHelper;

    private TestConfig testConfig;

    private Enclave enclave;

    public MigrateDataCommandTest(TestConfig testConfig) {
        this.testConfig = testConfig;
    }

    @Before
    public void beforeTest() throws Exception {

        FileUtils.copyDirectory(testConfig.getConfigDirPath().toFile(),outputDir.getRoot());

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
        if(testConfig.getOrionDbType() == OrionDbType.LEVELDB) {
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
            hikariConfig.setJdbcUrl("jdbc:postgresql://localhost:5432/namtruong");
            hikariConfig.setUsername("postgres");
            hikariConfig.setPassword("postgres");

            HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig);
            when(inboundDbHelper.getJdbcDataSource()).thenReturn(Optional.of(hikariDataSource));
            when(inboundDbHelper.getInputType()).thenReturn(InputType.JDBC);

            try(Connection connection = hikariDataSource.getConnection()) {
                try(Statement statement = connection.createStatement()) {

                    statement.execute("DROP TABLE IF EXISTS store");

                    statement.execute("CREATE TABLE store (\n" +
                        "  key char(60),\n" +
                        "  value bytea,\n" +
                        "  primary key(key)\n" +
                        ")");
                }

                Path sqlFile = workfingOrionConfigDir.resolve("sql").resolve("store.sql");

                try(Statement statement = connection.createStatement()) {

                    Files.lines(sqlFile).forEach(line -> {
                        try {
                            statement.addBatch(line);
                        } catch (SQLException sqlException) {
                            throw new RuntimeException(sqlException);
                        }
                    });
                    statement.executeBatch();
                }

                try(ResultSet count = connection.createStatement()
                    .executeQuery("SELECT COUNT(*) FROM store")) {
                    assertThat(count.next()).isTrue();
                    assertThat(count.getLong(1)).isEqualTo(Files.lines(sqlFile).count());
                }
            }
        }

        tesseraJdbcOptions = mock(TesseraJdbcOptions.class);
        when(tesseraJdbcOptions.getAction()).thenReturn("drop-and-create");
        when(tesseraJdbcOptions.getUrl()).thenReturn(tesseraJdbcUrl);
        when(tesseraJdbcOptions.getUsername()).thenReturn("junit");
        when(tesseraJdbcOptions.getPassword()).thenReturn("junit");

        orionKeyHelper = OrionKeyHelper.from(adjustedOrionConfigFile);

        migrateDataCommand = new MigrateDataCommand(inboundDbHelper, tesseraJdbcOptions, orionKeyHelper);

        MigrationInfoFactory.create(inboundDbHelper);

        enclave = createEnclave(orionKeyHelper);
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

        JdbcDataSource tesseraDataSource = new JdbcDataSource();
        tesseraDataSource.setURL(tesseraJdbcUrl);
        tesseraDataSource.setUser("junit");
        tesseraDataSource.setPassword("junit");

        if (!testConfig.getPrivacyGroupFixtures().isEmpty()) {
            try(Connection connection = tesseraDataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT DATA FROM PRIVACY_GROUP WHERE ID = ?")
            ) {

                for(PrivacyGroupFixture fixture : testConfig.getPrivacyGroupFixtures()) {
                    PrivacyGroup.Id id = PrivacyGroup.Id.fromBase64String(fixture.getId());
                    statement.setBytes(1, id.getBytes());
                    try(ResultSet resultSet = statement.executeQuery()) {
                        assertThat(resultSet.next()).isTrue();

                        byte[] payload = resultSet.getBytes(1);

                        PrivacyGroup privacyGroup = PrivacyGroupUtil.create().decode(payload);

                        assertThat(privacyGroup.getId().getBase64()).isEqualTo(fixture.getId());
                        assertThat(privacyGroup.getType().name()).isEqualTo(fixture.getType());

                        List<String> membersBase64 = privacyGroup.getMembers().stream()
                            .map(PublicKey::encodeToBase64)
                            .collect(Collectors.toList());
                        assertThat(membersBase64).isEqualTo(fixture.getMembers());
                    }
                }
            }
        }

        if (!testConfig.getEncryptedTransactionFixtures().isEmpty()) {
            try(Connection connection = tesseraDataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT ENCODED_PAYLOAD FROM ENCRYPTED_TRANSACTION WHERE HASH = ?")
            ) {

                for(EncryptedTransactionFixture fixture : testConfig.getEncryptedTransactionFixtures()) {
                    byte[] hash = Base64.getDecoder().decode(fixture.getId());
                    String expected = fixture.getPayload();
                    statement.setBytes(1,hash);
                    try(ResultSet resultSet = statement.executeQuery()) {
                        assertThat(resultSet.next()).isTrue();

                        byte[] payload = resultSet.getBytes(1);

                        EncodedPayload encodedPayload = PayloadEncoder.create().decode(payload);
                        PublicKey encryptionKey = orionKeyHelper.getKeyPairs().stream().findFirst()
                            .map(Box.KeyPair::publicKey)
                            .map(Box.PublicKey::bytesArray)
                            .map(PublicKey::from)
                            .get();

                        byte[] unencryptedTransaction = enclave.unencryptTransaction(encodedPayload,encryptionKey);

                        assertThat(unencryptedTransaction).isEqualTo(Base64.getDecoder().decode(expected));
                        assertThat(encodedPayload.getPrivacyGroupId()).isPresent();
                        assertThat(encodedPayload.getPrivacyGroupId().get().getBase64())
                            .isEqualTo(fixture.getPrivacyGroupId());
                        assertThat(encodedPayload.getSenderKey().encodeToBase64())
                            .isEqualTo(fixture.getSender());
                    }
                }
            }
        }
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
            }).map(p -> new TestConfig(OrionDbType.LEVELDB,p, Collections.emptyList(), Collections.emptyList()))
            .collect(Collectors.toUnmodifiableList());


        List<TestConfig> postgresConfigs = Files.list(Paths.get("pgsamples"))
            .filter(Files::isDirectory)
            .flatMap(d -> {
                try {
                    return Files.list(d).filter(Files::isDirectory);
                } catch (IOException ioException) {
                    throw new UncheckedIOException(ioException);
                }
            }).map(p -> new TestConfig(OrionDbType.POSTGRES,p,
                List.of(
                    new PrivacyGroupFixture(
                        "67NmE7/94nuomQiZv/g19BzyhhX84kwJo3lr5+n43xI=",
                        "LEGACY",
                        List.of("KkOjNLmCI6r+mICrC6l+XuEDjFEzQllaMQMpWLl4y1s=","qaBVuA+nG7Yt+kru6CGI2VMxOBAK7b1KNmiJuInHtwc=","GGilEkXLaQ9yhhtbpBT03Me9iYa7U/mWXxrJhnbl1XY=")
                    )
                ),
                List.of(
                    new EncryptedTransactionFixture(
                        "Tg+L+iUtsIKkaph25HCyCKpv+sYoK75dEhc7dLWAOQ0=",
                        "GGilEkXLaQ9yhhtbpBT03Me9iYa7U/mWXxrJhnbl1XY=",
                        "K1FKTUdZQ0RMY2JBZ0lDNUFhNWdnR0JBVWpTQUZXRUFFRmRnQUlEOVcxQmdRRkZoQVk0NEE0QmhBWTZET1lHQkFXQkFVbUFnZ1JBVllRQXpWMkFBZ1AxYmdRR1FnSUJSa0dBZ0FaQ1NrWkJRVUZDQVlBQ0JrRlZRZjRXK29SMkd6dnNXVTNUZzl5ZTZ6eUhjTDA2b0ZrazVnZXozTGMreUVxUVFnV0JBVVlDQ2dWSmdJQUdSVUZCZ1FGR0FrUU9Rb1ZCZy9ZQmhBSkZnQURsZ0FQUCtZSUJnUUZJMGdCVmdEMWRnQUlEOVcxQmdCRFlRWURKWFlBQTFZT0FjZ0dOZy9rZXhGR0EzVjRCamJVem1QQlJnWWxkYllBQ0EvVnRnWUdBRWdEWURZQ0NCRUJWZ1MxZGdBSUQ5VzRFQmtJQ0FOWkJnSUFHUWtwR1FVRkJRWUg1V1d3QmJZR2hndjFaYllFQlJnSUtCVW1BZ0FaRlFVR0JBVVlDUkE1RHpXNEJnQUlHUVZWQi9oYjZoSFliTyt4WlRkT0QzSjdyUElkd3ZUcWdXU1RtQjdQY3R6N0lTcEJDQllFQlJnSUtCVW1BZ0FaRlFVR0JBVVlDUkE1Q2hVRlpiWUFDQVZKQlFrRmIrb21WaWVucHlNVmdnZHpXakxhcDJjRm5kSXc3bmNZNjM4Si96WEtpNlZDU2JVK29jTGhLNWo0VmtjMjlzWTBNQUJSRUFNZ0FBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQmdnL25vSlh6K2RXM0x4NTJ0UW9LVXFGYjdiMG5vVjEvRlUxcG85YjhZMS9rTGtLdG9GUGYvMXN6cW95NzV5YUwxYVhmTE1uVTVWMGlmaE5VYVBiM2wwSHZVTmhHb0Job3BSSkZ5MmtQY29ZYlc2UVU5TnpIdlltR3UxUDVsbDhheVlaMjVkVjI0YUFxUTZNMHVZSWpxdjZZZ0tzTHFYNWU0UU9NVVROQ1dWb3hBeWxZdVhqTFc0cHlaWE4wY21samRHVms=",
                        "OGD/4dkDZWb4VqgDfElovjYMDAcSiRUiB6fLtFRmugU="
                    )
                )))
            .collect(Collectors.toUnmodifiableList());

        return List.copyOf(postgresConfigs);
    }

    static class TestConfig {
        private OrionDbType orionDbType;

        private Path configDirPath;

        private List<PrivacyGroupFixture> privacyGroupFixtures;

        private List<EncryptedTransactionFixture> encryptedTransactionFixtures;

        public TestConfig(OrionDbType orionDbType,
                          Path configDirPath,
                          List<PrivacyGroupFixture> privacyGroupFixtures,
                          List<EncryptedTransactionFixture> encryptedTransactionFixtures) {
            this.orionDbType = orionDbType;
            this.configDirPath = configDirPath;
            this.privacyGroupFixtures = privacyGroupFixtures;
            this.encryptedTransactionFixtures = encryptedTransactionFixtures;
        }

        public OrionDbType getOrionDbType() {
            return orionDbType;
        }

        public Path getConfigDirPath() {
            return configDirPath;
        }

        public List<PrivacyGroupFixture> getPrivacyGroupFixtures() {
            return privacyGroupFixtures;
        }

        public List<EncryptedTransactionFixture> getEncryptedTransactionFixtures() {
            return encryptedTransactionFixtures;
        }
    }

    static class PrivacyGroupFixture {

        private String id;
        private String type;
        private List<String> members;

        public PrivacyGroupFixture(String id, String type, List<String> members) {
            this.id = id;
            this.type = type;
            this.members = members;
        }

        public String getId() {
            return id;
        }

        public String getType() {
            return type;
        }

        public List<String> getMembers() {
            return members;
        }
    }

    static class EncryptedTransactionFixture {
        private String id;
        private String sender;
        private String payload;
        private String privacyGroupId;

        public EncryptedTransactionFixture(String id, String sender, String payload, String privacyGroupId) {
            this.id = id;
            this.sender = sender;
            this.payload = payload;
            this.privacyGroupId = privacyGroupId;
        }

        public String getId() {
            return id;
        }

        public String getSender() {
            return sender;
        }

        public String getPayload() {
            return payload;
        }

        public String getPrivacyGroupId() {
            return privacyGroupId;
        }
    }


    enum OrionDbType {
        LEVELDB,
        POSTGRES,
        ORACLE
    }

    private static Enclave createEnclave(OrionKeyHelper orionKeyHelper) {
        Config tesseraConfig = new Config();
        EncryptorConfig tesseraEncryptorConfig = new EncryptorConfig();
        tesseraEncryptorConfig.setType(EncryptorType.NACL);

        tesseraConfig.setKeys(new KeyConfiguration());

        KeyData keyData = orionKeyHelper.getKeyPairs().stream().map(p -> {
            KeyData keyData1 = new KeyData();
            keyData1.setPrivateKey(Base64.getEncoder().encodeToString(p.secretKey().bytesArray()));
            keyData1.setPublicKey(Base64.getEncoder().encodeToString(p.publicKey().bytesArray()));
            return keyData1;
        }).findFirst().get();

        tesseraConfig.getKeys().setKeyData(List.of(keyData));
        tesseraConfig.setEncryptor(tesseraEncryptorConfig);

        return EnclaveFactory.create().create(tesseraConfig);
    }


}
