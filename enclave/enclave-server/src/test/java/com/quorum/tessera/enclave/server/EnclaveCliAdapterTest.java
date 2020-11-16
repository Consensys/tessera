package com.quorum.tessera.enclave.server;

import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.cli.CliType;
import com.quorum.tessera.cli.keypassresolver.KeyPasswordResolver;
import com.quorum.tessera.config.Config;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.mockito.MockedStatic;
import picocli.CommandLine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class EnclaveCliAdapterTest {

    @Rule
    public SystemErrRule systemErrOutput = new SystemErrRule().enableLog();

    @Rule
    public SystemOutRule systemOutOutput = new SystemOutRule().enableLog();

    private CommandLine commandLine;

    private MockedStatic<KeyPasswordResolver> mockedStaticKeyPasswordResolver;

    private KeyPasswordResolver keyPasswordResolver;

    private CommandLine.ITypeConverter<Config> configConvertor;

    private EnclaveCliAdapter enclaveCliAdapter;

    @Before
    public void onSetUp() {
        keyPasswordResolver = mock(KeyPasswordResolver.class);

        mockedStaticKeyPasswordResolver = mockStatic(KeyPasswordResolver.class);
        mockedStaticKeyPasswordResolver.when(KeyPasswordResolver::create).thenReturn(keyPasswordResolver);

        System.setProperty(CliType.CLI_TYPE_KEY, CliType.ENCLAVE.name());
        this.systemErrOutput.clearLog();

        configConvertor = mock(CommandLine.ITypeConverter.class);

        enclaveCliAdapter = new EnclaveCliAdapter();

        commandLine = new CommandLine(enclaveCliAdapter);
        commandLine
            .registerConverter(Config.class, configConvertor)
            .setSeparator(" ")
            .setCaseInsensitiveEnumValuesAllowed(true);
    }

    @After
    public void onTearDown() {
        try {

            verifyNoMoreInteractions(configConvertor);
            verifyNoMoreInteractions(keyPasswordResolver);
            System.clearProperty(CliType.CLI_TYPE_KEY);
            mockedStaticKeyPasswordResolver.verify(KeyPasswordResolver::create);
            mockedStaticKeyPasswordResolver.verifyNoMoreInteractions();
        } finally {
            mockedStaticKeyPasswordResolver.close();
        }
    }

    @Test
    public void getType() {
        assertThat(enclaveCliAdapter.getType()).isEqualTo(CliType.ENCLAVE);
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

    @Test
    public void configPassedToResolver() throws Exception {

            final String configFile = "myconfig";
            final Config config = mock(Config.class);

            when(configConvertor.convert(configFile)).thenReturn(config);

            mockedStaticKeyPasswordResolver.when(KeyPasswordResolver::create)
                .thenReturn(keyPasswordResolver);

            commandLine
                .execute("-configfile", configFile);

            final CliResult result = commandLine.getExecutionResult();

            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(0);
            assertThat(result.isSuppressStartup()).isFalse();
            assertThat(result.getConfig()).containsSame(config);

            verify(configConvertor).convert(configFile);

            verify(keyPasswordResolver).resolveKeyPasswords(config);

            mockedStaticKeyPasswordResolver.verify(KeyPasswordResolver::create);


    }
}
