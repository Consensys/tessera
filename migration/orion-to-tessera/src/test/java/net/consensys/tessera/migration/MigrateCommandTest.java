package net.consensys.tessera.migration;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import com.quorum.tessera.config.*;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.encryption.PublicKey;
import net.consensys.tessera.migration.data.MigrationInfo;
import org.apache.tuweni.crypto.sodium.Box;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class MigrateCommandTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MigrateCommandTest.class);

    @Rule
    public TemporaryFolder outputDir = new TemporaryFolder();

    private String[] args;

    private MigrateTestConfig migrateTestConfig;

    private String tesseraJdbcUrl;

    private OrionKeyHelper orionKeyHelper;

    private Enclave enclave;

    public MigrateCommandTest(MigrateTestConfig migrateTestConfig) {
        this.migrateTestConfig = migrateTestConfig;
    }

    @Before
    public void beforeTest() throws Exception {

        Path orionConfigFilePath = migrateTestConfig.getOrionConfigDir().resolve("orion.conf");

        assertThat(orionConfigFilePath).exists();

        Toml toml = new Toml().read(orionConfigFilePath.toFile());
        Path adjustedOrionConfigFile = migrateTestConfig.getOrionConfigDir().resolve("orion-adjusted.conf");

        TomlWriter tomlWriter = new TomlWriter.Builder().build();
        Map m = new HashMap(toml.toMap());
        Path workdir = Path.of("").toAbsolutePath();
        m.put("workdir",workdir.toString());
        m.put("storage","leveldb:"+ workdir +"/"+ migrateTestConfig.getOrionConfigDir().resolve("routerdb").toString());
        tomlWriter.write(m,Files.newOutputStream(adjustedOrionConfigFile));

        Files.lines(adjustedOrionConfigFile).forEach(System.out::println);

        Path tesseraConfigFile = Paths.get(outputDir.getRoot().getAbsolutePath(),"tessera-config.json");

        Path pwd = outputDir.getRoot().toPath();
        this.tesseraJdbcUrl = "jdbc:h2:" + pwd + "/" + UUID.randomUUID().toString() + ".db";

        List<String> argsList = List.of(
                "-f",adjustedOrionConfigFile.toString(),
                "-o",tesseraConfigFile.toString(),
                "tessera.jdbc.user=junit",
                "tessera.jdbc.password=junit",
                "tessera.db.action=create",
                "tessera.jdbc.url=".concat(tesseraJdbcUrl)
        );

        this.args = argsList.toArray(String[]::new);

        LOGGER.info("Args: {}", Arrays.toString(args));

        orionKeyHelper = OrionKeyHelper.from(adjustedOrionConfigFile);

        enclave = createEnclave(orionKeyHelper);

    }

    @After
    public void afterTest() {
        MigrationInfo.clear();
    }

    @Test
    public void migrate() throws Exception {


        MigrateCommand migrateCommand = new MigrateCommand();

        CommandLine commandLine = new CommandLine(migrateCommand)
            .setCaseInsensitiveEnumValuesAllowed(true);

        commandLine.registerConverter(OrionKeyHelper.class, new OrionKeyHelperConvertor());

        int exitCode = commandLine.execute(args);

        assertThat(exitCode).isZero();

        if(migrateTestConfig.getOutcomeFixtures().isEmpty()) {
            return;
        }

        JdbcDataSource tesseraDataSource = new JdbcDataSource();
        tesseraDataSource.setURL(tesseraJdbcUrl);
        tesseraDataSource.setUser("junit");
        tesseraDataSource.setPassword("junit");

        try(Connection connection = tesseraDataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT ENCODED_PAYLOAD FROM ENCRYPTED_TRANSACTION WHERE HASH = ?")
        ) {

            for(Fixture fixture : migrateTestConfig.getOutcomeFixtures()) {
                byte[] hash = Base64.getDecoder().decode(fixture.getHash());
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
                        .isEqualTo(fixture.getPrivacyGroup());
                    assertThat(encodedPayload.getSenderKey().encodeToBase64())
                        .isEqualTo(fixture.getSender());
                }
            }
        }
    }

    @Parameterized.Parameters(name = "{0}")
    public static List<MigrateTestConfig> configs() {
        return List.of(
            new MigrateTestConfig(Paths.get("samples", "10k", "orion"),List.of())
        );
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

    static class MigrateTestConfig {

        private Path orionConfigDir;

        private List<Fixture> outcomeFixtures;

        MigrateTestConfig(Path orionConfigDir,List<Fixture> outcomeFixtures) {
            this.orionConfigDir = orionConfigDir;
            this.outcomeFixtures = outcomeFixtures;
        }

        public Path getOrionConfigDir() {
            return orionConfigDir;
        }

        public List<Fixture> getOutcomeFixtures() {
            return outcomeFixtures;
        }

        @Override
        public String toString() {
            return "MigrateTestConfig{" +
                "orionConfigDir=" + orionConfigDir +
                '}';
        }
    }

    static class Fixture {

        private String hash;

        private String payload;

        private String sender;

        private String privacyGroup;

        Fixture(String hash, String payload, String sender, String privacyGroup) {
            this.hash = hash;
            this.payload = payload;
            this.sender = sender;
            this.privacyGroup = privacyGroup;
        }

        public String getHash() {
            return hash;
        }

        public String getPayload() {
            return payload;
        }

        public String getSender() {
            return sender;
        }

        public String getPrivacyGroup() {
            return privacyGroup;
        }

    }

}
