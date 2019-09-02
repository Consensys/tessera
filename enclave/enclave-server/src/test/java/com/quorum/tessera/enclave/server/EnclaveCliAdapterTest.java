package com.quorum.tessera.enclave.server;

import com.quorum.tessera.cli.CliDelegate;
import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.cli.CliType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class EnclaveCliAdapterTest {

    @Rule public SystemErrRule systemErrOutput = new SystemErrRule().enableLog();

    @Rule public SystemOutRule systemOutOutput = new SystemOutRule().enableLog();

    @Before
    public void onSetUp() {
        this.systemErrOutput.clearLog();
    }

    @Test
    public void getType() {
        assertThat(new EnclaveCliAdapter().getType()).isEqualTo(CliType.ENCLAVE);
    }

    @Test
    public void missingConfigurationOutputsErrorMessage() throws Exception {
        final CliResult result = CliDelegate.instance().execute();

        final String output = systemErrOutput.getLog();

        assertThat(result).isEqualToComparingFieldByField(new CliResult(0, true, null));
        assertThat(output).contains("Missing required option '-configfile <config>'");
    }

    @Test
    public void helpOptionOutputsUsageMessage() throws Exception {
        final CliResult result = CliDelegate.instance().execute("help");

        final String output = systemOutOutput.getLog();

        assertThat(result).isEqualToComparingFieldByField(new CliResult(0, true, null));
        assertThat(output)
                .contains(
                        "Usage:",
                        "Run a standalone enclave to perform encryption/decryption operations",
                        "<main class> [help] -configfile <config> [-pidfile <pidFilePath>]");

        assertThat(output)
                .contains(
                        "Description:",
                        "Run a standalone enclave, which will perform encryption/decryption operations",
                        "for a transaction manager.This means that the transation manager does not",
                        "perform any of the operations inside its own process, shielding the user from");

        assertThat(output)
                .contains(
                        "Options:",
                        "      -configfile <config>   path to configuration file",
                        "      help                   display this help message",
                        "      -pidfile <pidFilePath> the path to write the PID to");
    }

    @Test
    public void configPassedToResolver() throws Exception {
        final Path inputFile = Paths.get(getClass().getResource("/sample-config.json").toURI());

        final CliResult result = CliDelegate.instance().execute("-configfile", inputFile.toString());

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(0);
        assertThat(result.isSuppressStartup()).isFalse();
        assertThat(result.getConfig()).isPresent();

        assertThat(MockKeyPasswordResolver.getSeen()).isNotNull();
    }
}
