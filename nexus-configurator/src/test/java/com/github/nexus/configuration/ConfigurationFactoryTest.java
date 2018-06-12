package com.github.nexus.configuration;

import org.apache.commons.cli.UnrecognizedOptionException;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class ConfigurationFactoryTest {

    private ConfigurationFactory factory;

    @Before
    public void init() throws NoSuchMethodException {
        this.factory = new ConfigurationFactory();
    }

    @Test
    public void configFilePulledFromCliArgs() {

        final String[] args = new String[]{"--configfile", "/tmp/configFile.yml"};

        final List<Path> configPath = ConfigurationFactory.getConfigFilePath(args);

        assertThat(configPath).hasSize(1).containsExactly(Paths.get("/tmp/configFile.yml"));

    }

    @Test
    public void configFileSkippedIfNotFound() {
        final List<Path> configPath = ConfigurationFactory.getConfigFilePath();

        assertThat(configPath).hasSize(0);

    }

    @Test
    public void validPropertySetSucceeds() {

        final String[] validProperties = new String[]{
            "--privateKeys", "path/to/private/key"
        };

        final Properties props = ConfigurationFactory.getCliProperties(validProperties);

        assertThat(props).containsKeys("privateKeys");

    }

    @Test
    public void invalidPropertySetThrowsException() {

        final String[] invalidProperties = new String[]{
            "---privateKeys", "path/to/private/key"
        };

        final Throwable throwable = catchThrowable(() -> ConfigurationFactory.getCliProperties(invalidProperties));

        assertThat(throwable).isInstanceOf(RuntimeException.class);
        assertThat(throwable.getCause())
            .isInstanceOf(UnrecognizedOptionException.class)
            .hasMessage("Unrecognized option: ---privateKeys");

    }

    @Test
    public void noPropertySetUsesDefaultProperty() throws NoSuchMethodException {

        final Configuration configuration = ConfigurationFactory.init();

        final String privateKeyList = configuration.privateKeys();
        assertThat(privateKeyList).isEqualTo("10,20");

        final List<String> publicKeyList = configuration.publicKeys();
        assertThat(publicKeyList).hasSize(1).containsExactly("5");

    }

    @Test
    public void fileProvidedUsesValuesFromFileInsteadOfDefault() throws NoSuchMethodException {

        ConfigurationFactory.cliArgsArray = new String[]{"--configfile", "./src/test/resources/other-config-file.yml"};

        final Configuration configuration = factory.init();

        final String privateKeyList = configuration.privateKeys();
        assertThat(privateKeyList).isEqualTo("other-config-file-value");

        final List<String> publicKeyList = configuration.publicKeys();
        assertThat(publicKeyList).hasSize(1).containsExactly("other-config-file-value");

    }

}
