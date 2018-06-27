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

public class Cfg4jPropertyLoaderTest {

    private Cfg4jPropertyLoader loader;

    @Before
    public void init() {
        this.loader = new Cfg4jPropertyLoader();
    }

    @Test
    public void serviceLoaderCreatesInstance() {

        final PropertyLoader propertyLoader = PropertyLoader.create();

        assertThat(propertyLoader).isInstanceOf(Cfg4jPropertyLoader.class);

    }

    @Test
    public void configFilePulledFromCliArgs() {

        final String[] args = new String[]{"--configfile", "/tmp/configFile.yml"};

        final List<Path> configPath = loader.getConfigFilePath(args);

        assertThat(configPath).hasSize(1).containsExactly(Paths.get("/tmp/configFile.yml"));

    }

    @Test
    public void configFileSkippedIfNotFound() {
        final List<Path> configPath = loader.getConfigFilePath();

        assertThat(configPath).hasSize(0);

    }

    @Test
    public void validPropertySetSucceeds() {

        final String[] validProperties = new String[]{
            "--privateKeys", "path/to/private/key"
        };

        final Properties props = loader.getCliProperties(validProperties);

        assertThat(props).containsKeys("privateKeys");

    }

    @Test
    public void invalidPropertySetThrowsException() {

        final String[] invalidProperties = new String[]{
            "---privateKeys", "path/to/private/key"
        };

        final Throwable throwable = catchThrowable(() -> loader.getCliProperties(invalidProperties));

        assertThat(throwable).isInstanceOf(RuntimeException.class);
        assertThat(throwable.getCause())
            .isInstanceOf(UnrecognizedOptionException.class)
            .hasMessage("Unrecognized option: ---privateKeys");

    }

    @Test
    public void noPropertySetUsesDefaultProperty() {

        final Properties props = loader.getAllProperties();

        final String privateKeys = props.getProperty("privateKeys");
        assertThat(privateKeys).isEqualTo("10,20");

        final String publicKeys = props.getProperty("publicKeys");
        assertThat(publicKeys).isEqualTo("5");

    }

    @Test
    public void fileProvidedUsesValuesFromFileInsteadOfDefault() {

        final String[] cliArgsArray = new String[]{"--configfile", "./src/test/resources/other-config-file.yml"};

        final Properties props = loader.getAllProperties(cliArgsArray);

        final String privateKeys = props.getProperty("privateKeys");
        assertThat(privateKeys).isEqualTo("other-config-file-value");

        final String publicKeys = props.getProperty("publicKeys");
        assertThat(publicKeys).isEqualTo("other-config-file-value");

    }

    @Test
    public void allPropertiesHaveValue() {

        final Properties props = loader.getAllProperties();

        assertThat(props).hasSize(ConfigurationParser.KNOWN_PROPERTIES.length);

    }

}
