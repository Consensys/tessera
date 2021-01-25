package net.consensys.tessera.migration;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.EncryptorType;
import com.quorum.tessera.config.util.JaxbUtil;
import net.consensys.tessera.migration.config.JdbcConfigBuilder;
import org.junit.Before;
import org.junit.Test;
import picocli.CommandLine;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class MigrateCommandTest {

    private Path orionConfigFile;

    private CommandLine commandLine;

    private MigrateCommand migrateCommand;

    private Path outputFile;

    @Before
    public void beforeTest() throws Exception {
        outputFile = Paths.get(UUID.randomUUID().toString());
        outputFile.toFile().deleteOnExit();
        orionConfigFile = Paths.get(getClass().getResource("/orion.conf").toURI()).toAbsolutePath();
        migrateCommand = new MigrateCommand();
        commandLine = new CommandLine(migrateCommand).setCaseInsensitiveEnumValuesAllowed(true);
        commandLine.registerConverter(OrionKeyHelper.class, new OrionKeyHelperConvertor());
    }

    @Test
    public void help() {
        commandLine.execute();
    }

    @Test
    public void levelDb() {

        String[] args =
                new String[] {
                    "tessera.jdbc.user", "junituser",
                    "tessera.jdbc.password", "junitpassword",
                    "tessera.jdbc.url", "jdbc:h2:./build/testdb;MODE=Oracle;TRACE_LEVEL_SYSTEM_OUT=0",
                    "tessera.db.action", "drop-and-create",
                    "orionconfig", orionConfigFile.toString(),
                    "outputfile", outputFile.toAbsolutePath().toString(),
                    "skipValidation"
                };

        CommandLine.ParseResult parseResult = commandLine.parseArgs(args);

        assertThat(parseResult.hasMatchedOption("orionconfig")).isTrue();

        int exitCode = commandLine.execute(args);
        assertThat(exitCode).isZero();
    }

    @Test
    public void loadFullConfigSample() {
        String file = getClass().getResource("/fullConfigTest.toml").getFile();

        String dbuser = "junituser";
        String dbpassword = "junitpassword";
        String dburl = "jdbc:h2:./build/testdb2;MODE=Oracle;TRACE_LEVEL_SYSTEM_OUT=0";

        String[] args =
                new String[] {
                    "tessera.jdbc.user", dbuser,
                    "tessera.jdbc.password", dbpassword,
                    "tessera.jdbc.url", dburl,
                    "tessera.db.action", "drop-and-create",
                    "orionconfig", file,
                    "outputfile", outputFile.toString(),
                    "skipValidation"
                };

        CommandLine.ParseResult result = commandLine.parseArgs(args);
        assertThat(result).isNotNull();

        int exitCode = commandLine.execute(args);
        assertThat(exitCode).isZero();
        Config config = commandLine.getExecutionResult();
        assertThat(config).isNotNull();
        assertThat(config.getEncryptor().getType()).isEqualTo(EncryptorType.NACL);
        assertThat(config.isBootstrapNode()).isFalse();
        assertThat(config.isDisablePeerDiscovery()).isFalse();
        assertThat(config.isUseWhiteList()).isFalse();


        JaxbUtil.marshalWithNoValidation(config, System.out);

        assertThat(config.getJdbcConfig())
                .isEqualTo(JdbcConfigBuilder.create().withUser(dbuser).withUrl(dburl).withPassword(dbpassword).build());
    }
}
