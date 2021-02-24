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


        JdbcDataSource tesseraDataSource = new JdbcDataSource();
        tesseraDataSource.setURL(tesseraJdbcUrl);
        tesseraDataSource.setUser("junit");
        tesseraDataSource.setPassword("junit");

        MigrationInfo migrationInfo = MigrationInfo.getInstance();

        try (
            Connection connection = tesseraDataSource.getConnection();
            ResultSet txnRs = connection.createStatement().executeQuery("SELECT COUNT(*) FROM ENCRYPTED_TRANSACTION");
            ResultSet privacyGroupRs = connection.createStatement().executeQuery("SELECT COUNT(*) FROM PRIVACY_GROUP")
        ) {

            assertThat(txnRs.next()).isTrue();
            assertThat(txnRs.getLong(1)).isEqualTo(migrationInfo.getTransactionCount());

            assertThat(privacyGroupRs.next()).isTrue();
            assertThat(privacyGroupRs.getLong(1)).isEqualTo(migrationInfo.getPrivacyGroupCount());
        }

        if(migrateTestConfig.getOutcomeFixtures().isEmpty()) {
            return;
        }


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
                    Enclave enclave = createEnclave();

                    PublicKey encryptionKey = orionKeyHelper.getKeyPairs().stream().findFirst()
                        .map(Box.KeyPair::publicKey)
                        .map(Box.PublicKey::bytesArray)
                        .map(PublicKey::from)
                        .get();

                    byte[] unencryptedTransaction = enclave.unencryptTransaction(encodedPayload,encryptionKey);

                    assertThat(unencryptedTransaction).isEqualTo(Base64.getDecoder().decode(expected));

                    assertThat(encodedPayload.getPrivacyGroupId()).isPresent();
                    assertThat(encodedPayload.getPrivacyGroupId().get().encodeToBase64())
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
            new MigrateTestConfig(Paths.get("samples", "10k", "orion"),List.of()),
            new MigrateTestConfig(Paths.get("samples", "100k", "orion"),
                List.of(
                    new Fixture(
                        "+NznOTG5M1xCr0H2YdH+guA5JZPZCQKarvAIPyWP/AQ=",
                        "K1JUNUFvQ0FnSUM1RkY5Z2dHQkFValNBRldJQUFCRlhZQUNBL1Z0UVlFQlJZZ0FUbnpnRGdHSUFFNStET1lHQkFXQkFVbUJBZ1JBVllnQUFOMWRnQUlEOVc0RUJrSUNBVVdCQVVaT1NrWkNFWkFFQUFBQUFnaEVWWWdBQVdGZGdBSUQ5VzRPQ0FaRlFZQ0NDQVlXQkVSVmlBQUJ2VjJBQWdQMWJnbEdHWUFHQ0FvTUJFV1FCQUFBQUFJSVJGeFZpQUFDTlYyQUFnUDFiZ0lOU1lDQ0RBWkpRVUZDUWdGR1FZQ0FCa0lDRGcyQUFXNE9CRUJWaUFBRERWNENDQVZHQmhBRlNZQ0NCQVpCUVlnQUFwbFpiVUZCUVVKQlFrSUVCa0dBZkZvQVZZZ0FBOFZlQWdnT0FVV0FCZzJBZ0EyRUJBQW9ER1JhQlVtQWdBWkZRVzFCZ1FGSmdJQUdBVVdCQVVaT1NrWkNFWkFFQUFBQUFnaEVWWWdBQkZWZGdBSUQ5VzRPQ0FaRlFZQ0NDQVlXQkVSVmlBQUVzVjJBQWdQMWJnbEdHWUFHQ0FvTUJFV1FCQUFBQUFJSVJGeFZpQUFGS1YyQUFnUDFiZ0lOU1lDQ0RBWkpRVUZDUWdGR1FZQ0FCa0lDRGcyQUFXNE9CRUJWaUFBR0FWNENDQVZHQmhBRlNZQ0NCQVpCUVlnQUJZMVpiVUZCUVVKQlFrSUVCa0dBZkZvQVZZZ0FCcmxlQWdnT0FVV0FCZzJBZ0EyRUJBQW9ER1JhQlVtQWdBWkZRVzFCZ1FGSlFVRkNCWUFPUWdGR1FZQ0FCa0dJQUFjMlNrWkJpQUFJTFZsdFFnR0FFa0lCUmtHQWdBWkJpQUFIbWtwR1FZZ0FDQzFaYlVHQVNZQVZnQUdFQkFBcUJWSUZnL3dJWkZwQ0RZUDhXQWhlUVZWQlFVR0lBQXJGV1c0S0FWR0FCZ1dBQkZoVmhBUUFDQXhaZ0FwQUVrR0FBVW1BZ1lBQWdrR0FmQVdBZ2tBU0JBWktDWUI4UVlnQUNUbGVBVVdEL0dSYURnQUVYaFZWaUFBSi9WbHVDZ0FGZ0FRR0ZWWUlWWWdBQ2YxZVJnZ0ZiZ29FUkZXSUFBbjVYZ2xHQ1ZaRmdJQUdSa0dBQkFaQmlBQUpoVmx0YlVKQlFZZ0FDanBHUVlnQUNrbFpiVUpCV1cxdUFnaEVWWWdBQ3JWZGdBSUZnQUpCVlVHQUJBV0lBQXBOV1cxQ1FWbHRoRU42QVlnQUN3V0FBT1dBQTgvNWdnR0JBVWpTQUZXRUFFRmRnQUlEOVcxQmdCRFlRWVFDcFYyQUFOV0RnSElCak9WQ1RVUkZoQUhGWGdHTTVVSk5SRkdFQ1dGZUFZM0NnZ2pFVVlRSzhWNEJqbGRpYlFSUmhBeFJYZ0dPa1Y4TFhGR0VEbDFlQVk2a0ZuTHNVWVFQN1Y0QmozV0x0UGhSaEJGOVhZUUNwVmx1QVl3YjkzZ01VWVFDdVY0QmpDVjZuc3hSaEFURlhnR01ZRmczZEZHRUJsVmVBWXlPNGN0MFVZUUd6VjRCak1UemxaeFJoQWpkWFcyQUFnUDFiWVFDMllRVFhWbHRnUUZHQWdHQWdBWUtCQTRKU2c0R0JVWUZTWUNBQmtWQ0FVWkJnSUFHUWdJT0RZQUJiZzRFUUZXRUE5bGVBZ2dGUmdZUUJVbUFnZ1FHUVVHRUEyMVpiVUZCUVVKQlFrSUVCa0dBZkZvQVZZUUVqVjRDQ0E0QlJZQUdEWUNBRFlRRUFDZ01aRm9GU1lDQUJrVkJiVUpKUVVGQmdRRkdBa1FPUTgxdGhBWDFnQklBMkEyQkFnUkFWWVFGSFYyQUFnUDFiZ1FHUWdJQTFjLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vRnBCZ0lBR1FrcEdRZ0RXUVlDQUJrSktSa0ZCUVVHRUZlVlpiWUVCUmdJSVZGWUZTWUNBQmtWQlFZRUJSZ0pFRGtQTmJZUUdkWVFXWFZsdGdRRkdBZ29GU1lDQUJrVkJRWUVCUmdKRURrUE5iWVFJZllBU0FOZ05nWUlFUUZXRUJ5VmRnQUlEOVc0RUJrSUNBTlhQLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy94YVFZQ0FCa0pLUmtJQTFjLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vRnBCZ0lBR1FrcEdRZ0RXUVlDQUJrSktSa0ZCUVVHRUZvVlpiWUVCUmdJSVZGWUZTWUNBQmtWQlFZRUJSZ0pFRGtQTmJZUUkvWVFaNlZsdGdRRkdBZ21EL0ZvRlNZQ0FCa1ZCUVlFQlJnSkVEa1BOYllRS2tZQVNBTmdOZ1FJRVFGV0VDYmxkZ0FJRDlXNEVCa0lDQU5YUC8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8veGFRWUNBQmtKS1JrSUExa0dBZ0FaQ1NrWkJRVUZCaEJwRldXMkJBVVlDQ0ZSV0JVbUFnQVpGUVVHQkFVWUNSQTVEelcyRUMvbUFFZ0RZRFlDQ0JFQlZoQXRKWFlBQ0EvVnVCQVpDQWdEVnovLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLzhXa0dBZ0FaQ1NrWkJRVUZCaEIwUldXMkJBVVlDQ2dWSmdJQUdSVUZCZ1FGR0FrUU9RODF0aEF4eGhCNHhXVzJCQVVZQ0FZQ0FCZ29FRGdsS0RnWUZSZ1ZKZ0lBR1JVSUJSa0dBZ0FaQ0FnNE5nQUZ1RGdSQVZZUU5jVjRDQ0FWR0JoQUZTWUNDQkFaQlFZUU5CVmx0UVVGQlFrRkNRZ1FHUVlCOFdnQlZoQTRsWGdJSURnRkZnQVlOZ0lBTmhBUUFLQXhrV2dWSmdJQUdSVUZ0UWtsQlFVR0JBVVlDUkE1RHpXMkVENDJBRWdEWURZRUNCRUJWaEE2MVhZQUNBL1Z1QkFaQ0FnRFZ6Ly8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy84V2tHQWdBWkNTa1pDQU5aQmdJQUdRa3BHUVVGQlFZUWd1Vmx0Z1FGR0FnaFVWZ1ZKZ0lBR1JVRkJnUUZHQWtRT1E4MXRoQkVkZ0JJQTJBMkJBZ1JBVllRUVJWMkFBZ1AxYmdRR1FnSUExYy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vL0ZwQmdJQUdRa3BHUWdEV1FZQ0FCa0pLUmtGQlFVR0VJKzFaYllFQlJnSUlWRllGU1lDQUJrVkJRWUVCUmdKRURrUE5iWVFUQllBU0FOZ05nUUlFUUZXRUVkVmRnQUlEOVc0RUJrSUNBTlhQLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy94YVFZQ0FCa0pLUmtJQTFjLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vRnBCZ0lBR1FrcEdRVUZCUVlRa1pWbHRnUUZHQWdvRlNZQ0FCa1ZCUVlFQlJnSkVEa1BOYllHQmdBNEJVWUFHQllBRVdGV0VCQUFJREZtQUNrQVNBWUI4QllDQ0FrUVFDWUNBQllFQlJrSUVCWUVCU2dKS1JrSUdCVW1BZ0FZS0FWR0FCZ1dBQkZoVmhBUUFDQXhaZ0FwQUVnQlZoQlc5WGdHQWZFR0VGUkZkaEFRQ0FnMVFFQW9OU2tXQWdBWkZoQlc5V1c0SUJrWkJnQUZKZ0lHQUFJSkJiZ1ZTQlVwQmdBUUdRWUNBQmdJTVJZUVZTVjRLUUEyQWZGb0lCa1Z0UVVGQlFVSkJRa0ZaYllBQmhCWTFoQllaaENhQldXNFNFWVFtb1ZsdGdBWkJRa3BGUVVGWmJZQUJnQWxTUVVKQldXMkFBWVFXdWhJU0VZUXVmVmx0aEJtK0VZUVc2WVFtZ1ZsdGhCbXFGWUVCUmdHQmdBV0JBVW9CZ0tJRlNZQ0FCWVJBVFlDaVJPV0FCWUFDTGMvLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy9GblAvLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vL3hhQlVtQWdBWkNCVW1BZ0FXQUFJR0FBWVFZZ1lRbWdWbHR6Ly8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy84V2MvLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy9Gb0ZTWUNBQmtJRlNZQ0FCWUFBZ1ZHRU9ZSkNTa1pCai8vLy8veFpXVzJFSnFGWmJZQUdRVUpPU1VGQlFWbHRnQUdBRllBQ1FWSkJoQVFBS2tBUmcveGFRVUpCV1cyQUFZUWM2WVFhZVlRbWdWbHVFWVFjMWhXQUJZQUJoQnE5aENhQldXM1AvLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vL3haei8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vOFdnVkpnSUFHUWdWSmdJQUZnQUNCZ0FJbHovLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLzhXYy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vL0ZvRlNZQ0FCa0lGU1lDQUJZQUFnVkdFUEdwQ1JrR1AvLy8vL0ZsWmJZUW1vVmx0Z0FaQlFrcEZRVUZaYllBQ0FZQUNEYy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vL0ZuUC8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8veGFCVW1BZ0FaQ0JVbUFnQVdBQUlGU1FVSkdRVUZaYllHQmdCSUJVWUFHQllBRVdGV0VCQUFJREZtQUNrQVNBWUI4QllDQ0FrUVFDWUNBQllFQlJrSUVCWUVCU2dKS1JrSUdCVW1BZ0FZS0FWR0FCZ1dBQkZoVmhBUUFDQXhaZ0FwQUVnQlZoQ0NSWGdHQWZFR0VIK1ZkaEFRQ0FnMVFFQW9OU2tXQWdBWkZoQ0NSV1c0SUJrWkJnQUZKZ0lHQUFJSkJiZ1ZTQlVwQmdBUUdRWUNBQmdJTVJZUWdIVjRLUUEyQWZGb0lCa1Z0UVVGQlFVSkJRa0ZaYllBQmhDUEZoQ0R0aENhQldXNFJoQ095RllFQlJnR0JnQVdCQVVvQmdKWUZTWUNBQllSQ0VZQ1dST1dBQllBQmhDR1ZoQ2FCV1czUC8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8veFp6Ly8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy84V2dWSmdJQUdRZ1ZKZ0lBRmdBQ0JnQUlwei8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vOFdjLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vRm9GU1lDQUJrSUZTWUNBQllBQWdWR0VPWUpDU2taQmovLy8vL3haV1cyRUpxRlpiWUFHUVVKS1JVRkJXVzJBQVlRa1BZUWtJWVFtZ1ZsdUVoR0VMbjFaYllBR1FVSktSVUZCV1cyQUFZQUZnQUlSei8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vOFdjLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vRm9GU1lDQUJrSUZTWUNBQllBQWdZQUNEYy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vL0ZuUC8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8veGFCVW1BZ0FaQ0JVbUFnQVdBQUlGU1FVSktSVUZCV1cyQUFNNUJRa0ZaYllBQnovLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLzhXZzNQLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy94WVVGV0VLTGxkZ1FGRi9DTU41b0FBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFDQlVtQUVBWUNBWUNBQmdvRURnbEpnSklGU1lDQUJnR0VRWUdBa2tUbGdRQUdSVUZCZ1FGR0FrUU9RL1Z0Z0FIUC8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8veGFDYy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vL0ZoUVZZUXEwVjJCQVVYOEl3M21nQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUlGU1lBUUJnSUJnSUFHQ2dRT0NVbUFpZ1ZKZ0lBR0FZUS9MWUNLUk9XQkFBWkZRVUdCQVVZQ1JBNUQ5VzRCZ0FXQUFoWFAvLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vL3haei8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vOFdnVkpnSUFHUWdWSmdJQUZnQUNCZ0FJUnovLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLzhXYy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vL0ZvRlNZQ0FCa0lGU1lDQUJZQUFnZ1pCVlVJRnovLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLzhXZzNQLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy94Wi9qRnZoNWV2c2ZWdlJUM0ZDZlI2RTg5MERGTUQzc2lrZVd5QUt5TWZEdVNXRFlFQlJnSUtCVW1BZ0FaRlFVR0JBVVlDUkE1Q2pVRkJRVmx0Z0FIUC8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8veGFEYy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vL0ZoUVZZUXdsVjJCQVVYOEl3M21nQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUlGU1lBUUJnSUJnSUFHQ2dRT0NVbUFsZ1ZKZ0lBR0FZUkE3WUNXUk9XQkFBWkZRVUdCQVVZQ1JBNUQ5VzJBQWMvLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy9Gb0p6Ly8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy84V0ZCVmhES3RYWUVCUmZ3akRlYUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBZ1ZKZ0JBR0FnR0FnQVlLQkE0SlNZQ09CVW1BZ0FZQmhENmhnSTVFNVlFQUJrVkJRWUVCUmdKRURrUDFiWVF5Mmc0T0RZUStpVmx0aERTR0JZRUJSZ0dCZ0FXQkFVb0JnSm9GU1lDQUJZUS90WUNhUk9XQUFnSWR6Ly8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy84V2MvLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy9Gb0ZTWUNBQmtJRlNZQ0FCWUFBZ1ZHRU9ZSkNTa1pCai8vLy8veFpXVzJBQWdJVnovLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLzhXYy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vL0ZvRlNZQ0FCa0lGU1lDQUJZQUFnZ1pCVlVHRU50SUZnQUlDRmMvLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy9GblAvLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vL3hhQlVtQWdBWkNCVW1BZ0FXQUFJRlJoRHhxUWtaQmovLy8vL3haV1cyQUFnSVJ6Ly8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy84V2MvLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy9Gb0ZTWUNBQmtJRlNZQ0FCWUFBZ2daQlZVSUZ6Ly8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy84V2czUC8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8veFovM2ZKU3JSdml5SnRwd3JCby9EZU5xcFVycC9GanhLRVdLUFZhVGZVanMrK0RZRUJSZ0lLQlVtQWdBWkZRVUdCQVVZQ1JBNUNqVUZCUVZsdGdBSU9ERVJXQ2tHRVBEVmRnUUZGL0NNTjVvQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUNCVW1BRUFZQ0FZQ0FCZ29FRGdsS0RnWUZSZ1ZKZ0lBR1JVSUJSa0dBZ0FaQ0FnNE5nQUZ1RGdSQVZZUTdTVjRDQ0FWR0JoQUZTWUNDQkFaQlFZUTYzVmx0UVVGQlFrRkNRZ1FHUVlCOFdnQlZoRHY5WGdJSURnRkZnQVlOZ0lBTmhBUUFLQXhrV2dWSmdJQUdSVUZ0UWtsQlFVR0JBVVlDUkE1RDlXMUNDaEFPUVVKT1NVRkJRVmx0Z0FJQ0NoQUdRVUlPQkVCVmhENWhYWUVCUmZ3akRlYUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBZ1ZKZ0JBR0FnR0FnQVlLQkE0SlNZQnVCVW1BZ0FZQi9VMkZtWlUxaGRHZzZJR0ZrWkdsMGFXOXVJRzkyWlhKbWJHOTNBQUFBQUFDQlVsQmdJQUdSVUZCZ1FGR0FrUU9RL1Z1QWtWQlFrcEZRVUZaYlVGQlFWdjVGVWtNeU1Eb2dkSEpoYm5ObVpYSWdkRzhnZEdobElIcGxjbThnWVdSa2NtVnpjMFZTUXpJd09pQmhjSEJ5YjNabElIUnZJSFJvWlNCNlpYSnZJR0ZrWkhKbGMzTkZVa015TURvZ2RISmhibk5tWlhJZ1lXMXZkVzUwSUdWNFkyVmxaSE1nWW1Gc1lXNWpaVVZTUXpJd09pQjBjbUZ1YzJabGNpQmhiVzkxYm5RZ1pYaGpaV1ZrY3lCaGJHeHZkMkZ1WTJWRlVrTXlNRG9nZEhKaGJuTm1aWElnWm5KdmJTQjBhR1VnZW1WeWJ5QmhaR1J5WlhOelJWSkRNakE2SUdGd2NISnZkbVVnWm5KdmJTQjBhR1VnZW1WeWJ5QmhaR1J5WlhOelJWSkRNakE2SUdSbFkzSmxZWE5sWkNCaGJHeHZkMkZ1WTJVZ1ltVnNiM2NnZW1WeWI2SmthWEJtYzFnaUVpRFVFNW10VXE0WGRNb3NsNlhzY3FDSnViZDhyTmwrckpsQ3B0cHI1SEMwQzJSemIyeGpRd0FHREFBekFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBRUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQWdBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFFVG1GdFpRQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQmxONWJXSnZiQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFnZy9vb0QydVZRalZNM3VhZGRYYk4xb3kwdDZsV3E1N05YZHRRWjFNcDZaNU5wVUJvSGhvblNlQVZXMXFxTmMzMzVoTGZ0MjBmQXBHWXc4VU5lS3g3d0t0YkFybm9JY0IwdDUvSXVWMTg4ZkwzNHJ3NE1HbEN4V1RoTEplTnIvcGZpZGEwUVFSb0pyb3lycXBQVWRmdHZMQXJnb0NzdFUzcE9JRWM0U3gvcUYvRnJORGxLenFpbkpsYzNSeWFXTjBaV1E9",
                        "hwHS3n8i5XXzx8vfivDgwaULFZOEsl42v+l+J1rRBBE=",
                        "mujKuqk9R1+28sCuCgKy1Tek4gRzhLH+oX8Ws0OUrOo="
                    ),
                    new Fixture(
                        "+Ny5WXVpgNxfLJl+WJBDFHNeOOvxWW79jwFw46OUQy0=",
                        "K1FIRUM0Q0FnSUM1QVNsZ2dHQkFValNBRldBUFYyQUFnUDFiVUdFQkNvQmhBQjlnQURsZ0FQUCtZSUJnUUZJMGdCVmdEMWRnQUlEOVcxQmdCRFlRWUNoWFlBQTFZT0FjZ0dOODlkcXdGR0F0VjF0Z0FJRDlXMkJXWUFTQU5nTmdJSUVRRldCQlYyQUFnUDFiZ1FHUWdJQTFrR0FnQVpDU2taQlFVRkJnV0ZaYkFGdUFZQUNBZ29KVUFaSlFVSUdRVlZCL09LeDRudFJGY25BWFpTZDhUUWx3OHRzY0dsY2UwNTZFTllDVnJrNnFWQ0F6Z21CQVVZQ0RjLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vRm5QLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy94YUJVbUFnQVlLQlVtQWdBWkpRVUZCZ1FGR0FrUU9Rb1ZCVy9xSmxZbnA2Y2pGWUlLc0hSN29lSER4NVVNWUQxb0gvZGhzWEFDVE5xWms4UC9LeXNMaTBTbERvWkhOdmJHTkRBQVVRQURLQ0QrZWc3anFtOGNSUFNHdXJYcGRvRkJZWno4aXpYLzlWdnE0TzJFblIxTG51SXFTZ0dNaHEzaGd0T2hHUzRxaHU4bmx1bGRJdTZYSXNLRXJXVjVKcFpoUnpkVzZnaHdIUzNuOGk1WFh6eDh2Zml2RGd3YVVMRlpPRXNsNDJ2K2wrSjFyUkJCSGhvQU5XbGJUTVN3bEI1Z1ZSMTZHYzh3WUQyMXY4SStXc1E2VnZWL0pmZFVocWluSmxjM1J5YVdOMFpXUT0=",
                        "hwHS3n8i5XXzx8vfivDgwaULFZOEsl42v+l+J1rRBBE=",
                        "bE2ccVtLFjZ5Qy9STIi1zVTfoHBPvOZGtmVt2vaDIFE="
                    )
                )),
            new MigrateTestConfig(Paths.get("samples", "120k", "orion"),List.of())
        );
    }

    private Enclave createEnclave() {
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
