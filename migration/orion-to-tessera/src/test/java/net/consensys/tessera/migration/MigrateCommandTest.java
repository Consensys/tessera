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
            new MigrateTestConfig(Paths.get("samples", "10k", "orion"),
                List.of(
                    new Fixture(
                        "2eNL3ZHnXDKQzZYMp+wXjirdapHym3oEQ3CxHoZ6ECw=",
                        "K1FIbWdJQ0FnSUM1QVNsZ2dHQkFValNBRldBUFYyQUFnUDFiVUdFQkNvQmhBQjlnQURsZ0FQUCtZSUJnUUZJMGdCVmdEMWRnQUlEOVcxQmdCRFlRWUNoWFlBQTFZT0FjZ0dOODlkcXdGR0F0VjF0Z0FJRDlXMkJXWUFTQU5nTmdJSUVRRldCQlYyQUFnUDFiZ1FHUWdJQTFrR0FnQVpDU2taQlFVRkJnV0ZaYkFGdUFZQUNBZ29KVUFaSlFVSUdRVlZCL09LeDRudFJGY25BWFpTZDhUUWx3OHRzY0dsY2UwNTZFTllDVnJrNnFWQ0F6Z21CQVVZQ0RjLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vRm5QLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy94YUJVbUFnQVlLQlVtQWdBWkpRVUZCZ1FGR0FrUU9Rb1ZCVy9xSmxZbnA2Y2pGWUlLc0hSN29lSER4NVVNWUQxb0gvZGhzWEFDVE5xWms4UC9LeXNMaTBTbERvWkhOdmJHTkRBQVVRQURLQ0QraWdhSjkrd3YrT3pZSDN5TlhsbWU1eFMwQ0tvbVVZL3p6RzFUd3dUR2tFVlQrZ2I0NUhiMTRKSlMrTXMvTi93SG9CV0xhNCthZDRBa0xqR0xxby96YWN5UStnQTFhVnRNeExDVUhtQlZIWG9aenpCZ1BiVy93ajVheERwVzlYOGw5MVNHcjRRcUNIQWRMZWZ5TGxkZlBIeTkrSzhPREJwUXNWazRTeVhqYS82WDRuV3RFRUVhQ1RiTmNTS2ZnaW4rb0VhVkdRbDZPY1paMC8xeU9RcjRNQzhvMWJmVXZZTDRweVpYTjBjbWxqZEdWaw==",
                        "A1aVtMxLCUHmBVHXoZzzBgPbW/wj5axDpW9X8l91SGo=",
                        "ojZ0DDr9zBAsRJq6fmuFv6uDDYXXONswJyLnogaG0YY="
                    ),
                    new Fixture(
                        "2fy1LUU8qqL8obX0QAX0k8Fn3mLp1FpvpmzlXfDhqnI=",
                        "K1FIRUFvQ0FnSUM1QVNsZ2dHQkFValNBRldBUFYyQUFnUDFiVUdFQkNvQmhBQjlnQURsZ0FQUCtZSUJnUUZJMGdCVmdEMWRnQUlEOVcxQmdCRFlRWUNoWFlBQTFZT0FjZ0dOODlkcXdGR0F0VjF0Z0FJRDlXMkJXWUFTQU5nTmdJSUVRRldCQlYyQUFnUDFiZ1FHUWdJQTFrR0FnQVpDU2taQlFVRkJnV0ZaYkFGdUFZQUNBZ29KVUFaSlFVSUdRVlZCL09LeDRudFJGY25BWFpTZDhUUWx3OHRzY0dsY2UwNTZFTllDVnJrNnFWQ0F6Z21CQVVZQ0RjLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vRm5QLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy94YUJVbUFnQVlLQlVtQWdBWkpRVUZCZ1FGR0FrUU9Rb1ZCVy9xSmxZbnA2Y2pGWUlLc0hSN29lSER4NVVNWUQxb0gvZGhzWEFDVE5xWms4UC9LeXNMaTBTbERvWkhOdmJHTkRBQVVRQURLQ0QraWdyRGp4L21CMlVFSGtaMjhmaDUwbmUrbklIY2xaVzh3byt1c2JvSTZDc1E2Z0Vsek5GaDVNYVkyYit5anRjRWxVL2hwZ0tCSU56Y245Ryt5MENSVDNGOUNnS28yYlZxRCtuTmxOWUw1RUU3eTNJZE9udmlmdGppaXpwalJ0K0hUdUZCdmhvQU5XbGJUTVN3bEI1Z1ZSMTZHYzh3WUQyMXY4SStXc1E2VnZWL0pmZFVocWluSmxjM1J5YVdOMFpXUT0=",
                        "Ko2bVqD+nNlNYL5EE7y3IdOnviftjiizpjRt+HTuFBs=",
                        "DyAOiF/ynpc+JXa2YAGB0bCitSlOMNm+ShmB/7M6C4w="
                    ),
                    new Fixture(
                        "47vp9Vdaz6AN+5aVrC31xKSY0jySZFW5tA3fPYvyURU=",
                        "K1FIRGdJQ0FnSUM1QVNsZ2dHQkFValNBRldBUFYyQUFnUDFiVUdFQkNvQmhBQjlnQURsZ0FQUCtZSUJnUUZJMGdCVmdEMWRnQUlEOVcxQmdCRFlRWUNoWFlBQTFZT0FjZ0dOODlkcXdGR0F0VjF0Z0FJRDlXMkJXWUFTQU5nTmdJSUVRRldCQlYyQUFnUDFiZ1FHUWdJQTFrR0FnQVpDU2taQlFVRkJnV0ZaYkFGdUFZQUNBZ29KVUFaSlFVSUdRVlZCL09LeDRudFJGY25BWFpTZDhUUWx3OHRzY0dsY2UwNTZFTllDVnJrNnFWQ0F6Z21CQVVZQ0RjLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vRm5QLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy94YUJVbUFnQVlLQlVtQWdBWkpRVUZCZ1FGR0FrUU9Rb1ZCVy9xSmxZbnA2Y2pGWUlLc0hSN29lSER4NVVNWUQxb0gvZGhzWEFDVE5xWms4UC9LeXNMaTBTbERvWkhOdmJHTkRBQVVRQURLQ0QrZWdVU1kyUnA2eFUvbldGbXdjbE5kVmFzRnduMmhJNXRGSXNpLzNwa3FWQk1XZ05QMFFBdUp6WEcvaTBLVy9FdVBWTktCenVPWmtLTnJjYis0OHptb3hxbE9nazJ6WEVpbjRJcC9xQkdsUmtKZWpuR1dkUDljamtLK0RBdktOVzMxTDJDK2dObm1zdzByRFRua2psZTZka2x4SWp6enBHZkovWVRRTEZkNzBxTkcyQS95S2NtVnpkSEpwWTNSbFpBPT0=",
                        "k2zXEin4Ip/qBGlRkJejnGWdP9cjkK+DAvKNW31L2C8=",
                        "Nnmsw0rDTnkjle6dklxIjzzpGfJ/YTQLFd70qNG2A/w="
                    )
                ))
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
