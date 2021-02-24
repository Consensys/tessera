package net.consensys.tessera.migration.data;

import com.quorum.tessera.config.*;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.io.IOCallback;
import net.consensys.tessera.migration.OrionKeyHelper;
import org.h2.jdbcx.JdbcDataSource;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fusesource.leveldbjni.JniDBFactory.factory;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Ignore
@RunWith(Parameterized.class)
public class MigrateDataCommandTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MigrateDataCommandTest.class);

    private MigrateDataCommand migrateDataCommand;

    private InboundDbHelper inboundDbHelper;

    private TesseraJdbcOptions tesseraJdbcOptions;

    private Path orionConfigDir;

    private String tesseraJdbcUrl;

    private Path pwd = Paths.get("").toAbsolutePath();

    private OrionKeyHelper orionKeyHelper;

    private MigrationInfo migrationInfo;

    public MigrateDataCommandTest(Path orionConfigDir) {
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

        inboundDbHelper = mock(InboundDbHelper.class);
        when(inboundDbHelper.getLevelDb()).thenReturn(Optional.of(leveldb));
        when(inboundDbHelper.getInputType()).thenReturn(InputType.LEVELDB);

        tesseraJdbcOptions = mock(TesseraJdbcOptions.class);
        when(tesseraJdbcOptions.getAction()).thenReturn("drop-and-create");
        when(tesseraJdbcOptions.getUrl()).thenReturn(tesseraJdbcUrl);
        when(tesseraJdbcOptions.getUsername()).thenReturn("junit");
        when(tesseraJdbcOptions.getPassword()).thenReturn("junit");

        orionKeyHelper = OrionKeyHelper.from(orionConfigFile);

        migrateDataCommand = new MigrateDataCommand(inboundDbHelper, tesseraJdbcOptions, orionKeyHelper);


        this.migrationInfo = MigrationInfoFactory.create(inboundDbHelper);

    }

    @After
    public void afterTest() {
        MigrationInfo.clear();
    }


    @Test
    public void migrate() throws Exception {
        assertThat(migrateDataCommand.call()).hasSize(2);

        MigrationInfo migrationInfo = MigrationInfo.getInstance();
        LOGGER.info(" {}", migrationInfo);

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

        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT ENCODED_PAYLOAD FROM ENCRYPTED_TRANSACTION WHERE HASH = ?")
        ) {
            byte[] hash = Base64.getDecoder().decode("+NznOTG5M1xCr0H2YdH+guA5JZPZCQKarvAIPyWP/AQ=");
            String expected = "K1JUNUFvQ0FnSUM1RkY5Z2dHQkFValNBRldJQUFCRlhZQUNBL1Z0UVlFQlJZZ0FUbnpnRGdHSUFFNStET1lHQkFXQkFVbUJBZ1JBVllnQUFOMWRnQUlEOVc0RUJrSUNBVVdCQVVaT1NrWkNFWkFFQUFBQUFnaEVWWWdBQVdGZGdBSUQ5VzRPQ0FaRlFZQ0NDQVlXQkVSVmlBQUJ2VjJBQWdQMWJnbEdHWUFHQ0FvTUJFV1FCQUFBQUFJSVJGeFZpQUFDTlYyQUFnUDFiZ0lOU1lDQ0RBWkpRVUZDUWdGR1FZQ0FCa0lDRGcyQUFXNE9CRUJWaUFBRERWNENDQVZHQmhBRlNZQ0NCQVpCUVlnQUFwbFpiVUZCUVVKQlFrSUVCa0dBZkZvQVZZZ0FBOFZlQWdnT0FVV0FCZzJBZ0EyRUJBQW9ER1JhQlVtQWdBWkZRVzFCZ1FGSmdJQUdBVVdCQVVaT1NrWkNFWkFFQUFBQUFnaEVWWWdBQkZWZGdBSUQ5VzRPQ0FaRlFZQ0NDQVlXQkVSVmlBQUVzVjJBQWdQMWJnbEdHWUFHQ0FvTUJFV1FCQUFBQUFJSVJGeFZpQUFGS1YyQUFnUDFiZ0lOU1lDQ0RBWkpRVUZDUWdGR1FZQ0FCa0lDRGcyQUFXNE9CRUJWaUFBR0FWNENDQVZHQmhBRlNZQ0NCQVpCUVlnQUJZMVpiVUZCUVVKQlFrSUVCa0dBZkZvQVZZZ0FCcmxlQWdnT0FVV0FCZzJBZ0EyRUJBQW9ER1JhQlVtQWdBWkZRVzFCZ1FGSlFVRkNCWUFPUWdGR1FZQ0FCa0dJQUFjMlNrWkJpQUFJTFZsdFFnR0FFa0lCUmtHQWdBWkJpQUFIbWtwR1FZZ0FDQzFaYlVHQVNZQVZnQUdFQkFBcUJWSUZnL3dJWkZwQ0RZUDhXQWhlUVZWQlFVR0lBQXJGV1c0S0FWR0FCZ1dBQkZoVmhBUUFDQXhaZ0FwQUVrR0FBVW1BZ1lBQWdrR0FmQVdBZ2tBU0JBWktDWUI4UVlnQUNUbGVBVVdEL0dSYURnQUVYaFZWaUFBSi9WbHVDZ0FGZ0FRR0ZWWUlWWWdBQ2YxZVJnZ0ZiZ29FUkZXSUFBbjVYZ2xHQ1ZaRmdJQUdSa0dBQkFaQmlBQUpoVmx0YlVKQlFZZ0FDanBHUVlnQUNrbFpiVUpCV1cxdUFnaEVWWWdBQ3JWZGdBSUZnQUpCVlVHQUJBV0lBQXBOV1cxQ1FWbHRoRU42QVlnQUN3V0FBT1dBQTgvNWdnR0JBVWpTQUZXRUFFRmRnQUlEOVcxQmdCRFlRWVFDcFYyQUFOV0RnSElCak9WQ1RVUkZoQUhGWGdHTTVVSk5SRkdFQ1dGZUFZM0NnZ2pFVVlRSzhWNEJqbGRpYlFSUmhBeFJYZ0dPa1Y4TFhGR0VEbDFlQVk2a0ZuTHNVWVFQN1Y0QmozV0x0UGhSaEJGOVhZUUNwVmx1QVl3YjkzZ01VWVFDdVY0QmpDVjZuc3hSaEFURlhnR01ZRmczZEZHRUJsVmVBWXlPNGN0MFVZUUd6VjRCak1UemxaeFJoQWpkWFcyQUFnUDFiWVFDMllRVFhWbHRnUUZHQWdHQWdBWUtCQTRKU2c0R0JVWUZTWUNBQmtWQ0FVWkJnSUFHUWdJT0RZQUJiZzRFUUZXRUE5bGVBZ2dGUmdZUUJVbUFnZ1FHUVVHRUEyMVpiVUZCUVVKQlFrSUVCa0dBZkZvQVZZUUVqVjRDQ0E0QlJZQUdEWUNBRFlRRUFDZ01aRm9GU1lDQUJrVkJiVUpKUVVGQmdRRkdBa1FPUTgxdGhBWDFnQklBMkEyQkFnUkFWWVFGSFYyQUFnUDFiZ1FHUWdJQTFjLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vRnBCZ0lBR1FrcEdRZ0RXUVlDQUJrSktSa0ZCUVVHRUZlVlpiWUVCUmdJSVZGWUZTWUNBQmtWQlFZRUJSZ0pFRGtQTmJZUUdkWVFXWFZsdGdRRkdBZ29GU1lDQUJrVkJRWUVCUmdKRURrUE5iWVFJZllBU0FOZ05nWUlFUUZXRUJ5VmRnQUlEOVc0RUJrSUNBTlhQLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy94YVFZQ0FCa0pLUmtJQTFjLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vRnBCZ0lBR1FrcEdRZ0RXUVlDQUJrSktSa0ZCUVVHRUZvVlpiWUVCUmdJSVZGWUZTWUNBQmtWQlFZRUJSZ0pFRGtQTmJZUUkvWVFaNlZsdGdRRkdBZ21EL0ZvRlNZQ0FCa1ZCUVlFQlJnSkVEa1BOYllRS2tZQVNBTmdOZ1FJRVFGV0VDYmxkZ0FJRDlXNEVCa0lDQU5YUC8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8veGFRWUNBQmtKS1JrSUExa0dBZ0FaQ1NrWkJRVUZCaEJwRldXMkJBVVlDQ0ZSV0JVbUFnQVpGUVVHQkFVWUNSQTVEelcyRUMvbUFFZ0RZRFlDQ0JFQlZoQXRKWFlBQ0EvVnVCQVpDQWdEVnovLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLzhXa0dBZ0FaQ1NrWkJRVUZCaEIwUldXMkJBVVlDQ2dWSmdJQUdSVUZCZ1FGR0FrUU9RODF0aEF4eGhCNHhXVzJCQVVZQ0FZQ0FCZ29FRGdsS0RnWUZSZ1ZKZ0lBR1JVSUJSa0dBZ0FaQ0FnNE5nQUZ1RGdSQVZZUU5jVjRDQ0FWR0JoQUZTWUNDQkFaQlFZUU5CVmx0UVVGQlFrRkNRZ1FHUVlCOFdnQlZoQTRsWGdJSURnRkZnQVlOZ0lBTmhBUUFLQXhrV2dWSmdJQUdSVUZ0UWtsQlFVR0JBVVlDUkE1RHpXMkVENDJBRWdEWURZRUNCRUJWaEE2MVhZQUNBL1Z1QkFaQ0FnRFZ6Ly8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy84V2tHQWdBWkNTa1pDQU5aQmdJQUdRa3BHUVVGQlFZUWd1Vmx0Z1FGR0FnaFVWZ1ZKZ0lBR1JVRkJnUUZHQWtRT1E4MXRoQkVkZ0JJQTJBMkJBZ1JBVllRUVJWMkFBZ1AxYmdRR1FnSUExYy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vL0ZwQmdJQUdRa3BHUWdEV1FZQ0FCa0pLUmtGQlFVR0VJKzFaYllFQlJnSUlWRllGU1lDQUJrVkJRWUVCUmdKRURrUE5iWVFUQllBU0FOZ05nUUlFUUZXRUVkVmRnQUlEOVc0RUJrSUNBTlhQLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy94YVFZQ0FCa0pLUmtJQTFjLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vRnBCZ0lBR1FrcEdRVUZCUVlRa1pWbHRnUUZHQWdvRlNZQ0FCa1ZCUVlFQlJnSkVEa1BOYllHQmdBNEJVWUFHQllBRVdGV0VCQUFJREZtQUNrQVNBWUI4QllDQ0FrUVFDWUNBQllFQlJrSUVCWUVCU2dKS1JrSUdCVW1BZ0FZS0FWR0FCZ1dBQkZoVmhBUUFDQXhaZ0FwQUVnQlZoQlc5WGdHQWZFR0VGUkZkaEFRQ0FnMVFFQW9OU2tXQWdBWkZoQlc5V1c0SUJrWkJnQUZKZ0lHQUFJSkJiZ1ZTQlVwQmdBUUdRWUNBQmdJTVJZUVZTVjRLUUEyQWZGb0lCa1Z0UVVGQlFVSkJRa0ZaYllBQmhCWTFoQllaaENhQldXNFNFWVFtb1ZsdGdBWkJRa3BGUVVGWmJZQUJnQWxTUVVKQldXMkFBWVFXdWhJU0VZUXVmVmx0aEJtK0VZUVc2WVFtZ1ZsdGhCbXFGWUVCUmdHQmdBV0JBVW9CZ0tJRlNZQ0FCWVJBVFlDaVJPV0FCWUFDTGMvLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy9GblAvLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vL3hhQlVtQWdBWkNCVW1BZ0FXQUFJR0FBWVFZZ1lRbWdWbHR6Ly8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy84V2MvLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy9Gb0ZTWUNBQmtJRlNZQ0FCWUFBZ1ZHRU9ZSkNTa1pCai8vLy8veFpXVzJFSnFGWmJZQUdRVUpPU1VGQlFWbHRnQUdBRllBQ1FWSkJoQVFBS2tBUmcveGFRVUpCV1cyQUFZUWM2WVFhZVlRbWdWbHVFWVFjMWhXQUJZQUJoQnE5aENhQldXM1AvLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vL3haei8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vOFdnVkpnSUFHUWdWSmdJQUZnQUNCZ0FJbHovLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLzhXYy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vL0ZvRlNZQ0FCa0lGU1lDQUJZQUFnVkdFUEdwQ1JrR1AvLy8vL0ZsWmJZUW1vVmx0Z0FaQlFrcEZRVUZaYllBQ0FZQUNEYy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vL0ZuUC8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8veGFCVW1BZ0FaQ0JVbUFnQVdBQUlGU1FVSkdRVUZaYllHQmdCSUJVWUFHQllBRVdGV0VCQUFJREZtQUNrQVNBWUI4QllDQ0FrUVFDWUNBQllFQlJrSUVCWUVCU2dKS1JrSUdCVW1BZ0FZS0FWR0FCZ1dBQkZoVmhBUUFDQXhaZ0FwQUVnQlZoQ0NSWGdHQWZFR0VIK1ZkaEFRQ0FnMVFFQW9OU2tXQWdBWkZoQ0NSV1c0SUJrWkJnQUZKZ0lHQUFJSkJiZ1ZTQlVwQmdBUUdRWUNBQmdJTVJZUWdIVjRLUUEyQWZGb0lCa1Z0UVVGQlFVSkJRa0ZaYllBQmhDUEZoQ0R0aENhQldXNFJoQ095RllFQlJnR0JnQVdCQVVvQmdKWUZTWUNBQllSQ0VZQ1dST1dBQllBQmhDR1ZoQ2FCV1czUC8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8veFp6Ly8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy84V2dWSmdJQUdRZ1ZKZ0lBRmdBQ0JnQUlwei8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vOFdjLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vRm9GU1lDQUJrSUZTWUNBQllBQWdWR0VPWUpDU2taQmovLy8vL3haV1cyRUpxRlpiWUFHUVVKS1JVRkJXVzJBQVlRa1BZUWtJWVFtZ1ZsdUVoR0VMbjFaYllBR1FVSktSVUZCV1cyQUFZQUZnQUlSei8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vOFdjLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vRm9GU1lDQUJrSUZTWUNBQllBQWdZQUNEYy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vL0ZuUC8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8veGFCVW1BZ0FaQ0JVbUFnQVdBQUlGU1FVSktSVUZCV1cyQUFNNUJRa0ZaYllBQnovLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLzhXZzNQLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy94WVVGV0VLTGxkZ1FGRi9DTU41b0FBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFDQlVtQUVBWUNBWUNBQmdvRURnbEpnSklGU1lDQUJnR0VRWUdBa2tUbGdRQUdSVUZCZ1FGR0FrUU9RL1Z0Z0FIUC8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8veGFDYy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vL0ZoUVZZUXEwVjJCQVVYOEl3M21nQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUlGU1lBUUJnSUJnSUFHQ2dRT0NVbUFpZ1ZKZ0lBR0FZUS9MWUNLUk9XQkFBWkZRVUdCQVVZQ1JBNUQ5VzRCZ0FXQUFoWFAvLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vL3haei8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vOFdnVkpnSUFHUWdWSmdJQUZnQUNCZ0FJUnovLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLzhXYy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vL0ZvRlNZQ0FCa0lGU1lDQUJZQUFnZ1pCVlVJRnovLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLzhXZzNQLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy94Wi9qRnZoNWV2c2ZWdlJUM0ZDZlI2RTg5MERGTUQzc2lrZVd5QUt5TWZEdVNXRFlFQlJnSUtCVW1BZ0FaRlFVR0JBVVlDUkE1Q2pVRkJRVmx0Z0FIUC8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8veGFEYy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vL0ZoUVZZUXdsVjJCQVVYOEl3M21nQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUlGU1lBUUJnSUJnSUFHQ2dRT0NVbUFsZ1ZKZ0lBR0FZUkE3WUNXUk9XQkFBWkZRVUdCQVVZQ1JBNUQ5VzJBQWMvLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy9Gb0p6Ly8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy84V0ZCVmhES3RYWUVCUmZ3akRlYUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBZ1ZKZ0JBR0FnR0FnQVlLQkE0SlNZQ09CVW1BZ0FZQmhENmhnSTVFNVlFQUJrVkJRWUVCUmdKRURrUDFiWVF5Mmc0T0RZUStpVmx0aERTR0JZRUJSZ0dCZ0FXQkFVb0JnSm9GU1lDQUJZUS90WUNhUk9XQUFnSWR6Ly8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy84V2MvLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy9Gb0ZTWUNBQmtJRlNZQ0FCWUFBZ1ZHRU9ZSkNTa1pCai8vLy8veFpXVzJBQWdJVnovLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLzhXYy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vL0ZvRlNZQ0FCa0lGU1lDQUJZQUFnZ1pCVlVHRU50SUZnQUlDRmMvLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy9GblAvLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vL3hhQlVtQWdBWkNCVW1BZ0FXQUFJRlJoRHhxUWtaQmovLy8vL3haV1cyQUFnSVJ6Ly8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy84V2MvLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy9Gb0ZTWUNBQmtJRlNZQ0FCWUFBZ2daQlZVSUZ6Ly8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy84V2czUC8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8veFovM2ZKU3JSdml5SnRwd3JCby9EZU5xcFVycC9GanhLRVdLUFZhVGZVanMrK0RZRUJSZ0lLQlVtQWdBWkZRVUdCQVVZQ1JBNUNqVUZCUVZsdGdBSU9ERVJXQ2tHRVBEVmRnUUZGL0NNTjVvQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUNCVW1BRUFZQ0FZQ0FCZ29FRGdsS0RnWUZSZ1ZKZ0lBR1JVSUJSa0dBZ0FaQ0FnNE5nQUZ1RGdSQVZZUTdTVjRDQ0FWR0JoQUZTWUNDQkFaQlFZUTYzVmx0UVVGQlFrRkNRZ1FHUVlCOFdnQlZoRHY5WGdJSURnRkZnQVlOZ0lBTmhBUUFLQXhrV2dWSmdJQUdSVUZ0UWtsQlFVR0JBVVlDUkE1RDlXMUNDaEFPUVVKT1NVRkJRVmx0Z0FJQ0NoQUdRVUlPQkVCVmhENWhYWUVCUmZ3akRlYUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBZ1ZKZ0JBR0FnR0FnQVlLQkE0SlNZQnVCVW1BZ0FZQi9VMkZtWlUxaGRHZzZJR0ZrWkdsMGFXOXVJRzkyWlhKbWJHOTNBQUFBQUFDQlVsQmdJQUdSVUZCZ1FGR0FrUU9RL1Z1QWtWQlFrcEZRVUZaYlVGQlFWdjVGVWtNeU1Eb2dkSEpoYm5ObVpYSWdkRzhnZEdobElIcGxjbThnWVdSa2NtVnpjMFZTUXpJd09pQmhjSEJ5YjNabElIUnZJSFJvWlNCNlpYSnZJR0ZrWkhKbGMzTkZVa015TURvZ2RISmhibk5tWlhJZ1lXMXZkVzUwSUdWNFkyVmxaSE1nWW1Gc1lXNWpaVVZTUXpJd09pQjBjbUZ1YzJabGNpQmhiVzkxYm5RZ1pYaGpaV1ZrY3lCaGJHeHZkMkZ1WTJWRlVrTXlNRG9nZEhKaGJuTm1aWElnWm5KdmJTQjBhR1VnZW1WeWJ5QmhaR1J5WlhOelJWSkRNakE2SUdGd2NISnZkbVVnWm5KdmJTQjBhR1VnZW1WeWJ5QmhaR1J5WlhOelJWSkRNakE2SUdSbFkzSmxZWE5sWkNCaGJHeHZkMkZ1WTJVZ1ltVnNiM2NnZW1WeWI2SmthWEJtYzFnaUVpRFVFNW10VXE0WGRNb3NsNlhzY3FDSnViZDhyTmwrckpsQ3B0cHI1SEMwQzJSemIyeGpRd0FHREFBekFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBRUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQWdBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFFVG1GdFpRQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQmxONWJXSnZiQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFnZy9vb0QydVZRalZNM3VhZGRYYk4xb3kwdDZsV3E1N05YZHRRWjFNcDZaNU5wVUJvSGhvblNlQVZXMXFxTmMzMzVoTGZ0MjBmQXBHWXc4VU5lS3g3d0t0YkFybm9JY0IwdDUvSXVWMTg4ZkwzNHJ3NE1HbEN4V1RoTEplTnIvcGZpZGEwUVFSb0pyb3lycXBQVWRmdHZMQXJnb0NzdFUzcE9JRWM0U3gvcUYvRnJORGxLenFpbkpsYzNSeWFXTjBaV1E9";
            statement.setBytes(1,hash);

            try(ResultSet resultSet = statement.executeQuery()) {
                assertThat(resultSet.next()).isTrue();
                byte[] payload = resultSet.getBytes(1);
              //  assertThat(Base64.getEncoder().encodeToString(payload)).isEqualTo(expected);
                final EncodedPayload encodedPayload = PayloadEncoder.create().decode(payload);

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

                Enclave enclave = EnclaveFactory.create().create(tesseraConfig);

               byte[] unencryptedTransaction = enclave.unencryptTransaction(encodedPayload,
                    Optional.of("A1aVtMxLCUHmBVHXoZzzBgPbW/wj5axDpW9X8l91SGo=")
                        .map(Base64.getDecoder()::decode)
                        .map(PublicKey::from).get());

                assertThat(unencryptedTransaction).isEqualTo(Base64.getDecoder().decode(expected));

                assertThat(encodedPayload.getPrivacyGroupId()).isPresent();
                assertThat(encodedPayload.getPrivacyGroupId().get().encodeToBase64())
                    .isEqualTo("mujKuqk9R1+28sCuCgKy1Tek4gRzhLH+oX8Ws0OUrOo=");

                assertThat(encodedPayload.getSenderKey().encodeToBase64())
                    .isEqualTo("hwHS3n8i5XXzx8vfivDgwaULFZOEsl42v+l+J1rRBBE=");
            }
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
