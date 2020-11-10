package com.quorum.tessera.enclave.server;

import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.cli.CliType;
import com.quorum.tessera.cli.parsers.ConfigConverter;
import com.quorum.tessera.config.Config;
import org.junit.*;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;
import picocli.CommandLine;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class EnclaveCliAdapterTest {

    @Rule public SystemErrRule systemErrOutput = new SystemErrRule().enableLog();

    @Rule public SystemOutRule systemOutOutput = new SystemOutRule().enableLog();

    private CommandLine commandLine;

    @Before
    public void onSetUp() {
        System.setProperty(CliType.CLI_TYPE_KEY, CliType.ENCLAVE.name());
        this.systemErrOutput.clearLog();

        commandLine = new CommandLine(new EnclaveCliAdapter());
        commandLine
            .registerConverter(Config.class, new ConfigConverter())
            .setSeparator(" ")
            .setCaseInsensitiveEnumValuesAllowed(true);
    }

    @After
    public void onTearDown() {
        System.clearProperty(CliType.CLI_TYPE_KEY);
    }

    @Test
    public void getType() {
        assertThat(new EnclaveCliAdapter().getType()).isEqualTo(CliType.ENCLAVE);
    }

    @Test
    public void missingConfigurationOutputsErrorMessage() {
        commandLine.execute();
        final CliResult result = commandLine.getExecutionResult();

        final String output = systemErrOutput.getLog();

        assertThat(result).isNull();
//        assertThat(result).isEqualToComparingFieldByField(new CliResult(1, true, null));
        assertThat(output).contains("Missing required option '-configfile <config>'");
    }

    @Test
    public void helpOptionOutputsUsageMessage() {
        commandLine.execute("help");
        final CliResult result = commandLine.getExecutionResult();

        final String output = systemOutOutput.getLog();

//        assertThat(result).isEqualToComparingFieldByField(new CliResult(0, true, null));
        assertThat(result).isNull();
        assertThat(output)
                .contains(
                        "Usage:",
                        "Run a standalone enclave to perform encryption/decryption operations",
                        "<main class> [help] -configfile <config> [-pidfile <pidFilePath>]");

        assertThat(output)
                .contains(
                        "Description:",
                        "Run a standalone enclave, which will perform encryption/decryption operations",
                        "for a transaction manager. This means that the transaction manager does not",
                        "perform any of the operations inside its own process, shielding the user from");

        assertThat(output)
                .contains(
                        "Options:",
                        "      -configfile <config>   path to configuration file",
                        "      help                   display this help message",
                        "      -pidfile <pidFilePath> the path to write the PID to");
    }

    @Ignore
    @Test
    public void configPassedToResolver() throws Exception {
        final Path inputFile = Paths.get(getClass().getResource("/sample-config.json").toURI());

        commandLine.execute("-configfile", inputFile.toString());
        final CliResult result = commandLine.getExecutionResult();

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(0);
        assertThat(result.isSuppressStartup()).isFalse();
        assertThat(result.getConfig()).isPresent();

        assertThat(MockKeyPasswordResolver.getSeen()).isNotNull();
    }
}
