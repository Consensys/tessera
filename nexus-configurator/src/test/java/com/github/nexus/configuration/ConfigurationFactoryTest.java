package com.github.nexus.configuration;

import org.apache.commons.cli.UnrecognizedOptionException;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class ConfigurationFactoryTest {

    @Test
    public void cannotInstantiateClass() {
        final Throwable throwable = catchThrowable(ConfigurationFactory::new);
        assertThat(throwable).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    public void validPropertySetSucceeds() {

        final String[] validProperties = new String[]{
            "--privateKeys", "path/to/private/key"
        };

        final Throwable throwable = catchThrowable(() -> ConfigurationFactory.cliParameters(validProperties));

        assertThat(throwable).isNull();

    }

    @Test
    public void invalidPropertySetThrowsException() {

        final String[] validProperties = new String[]{
            "---privateKeys", "path/to/private/key"
        };

        final Throwable throwable = catchThrowable(() -> ConfigurationFactory.cliParameters(validProperties));

        assertThat(throwable).isInstanceOf(RuntimeException.class);
        assertThat(throwable.getCause())
            .isInstanceOf(UnrecognizedOptionException.class)
            .hasMessage("Unrecognized option: ---privateKeys");

    }

    @Test
    public void noPropertySetUsesDefaultProperty() {

        final Configuration configuration = ConfigurationFactory.init();

        final List<String> privateKeyList = configuration.privateKeys();
        assertThat(privateKeyList).hasSize(2).containsExactly("10", "20");

        final List<String> publicKeyList = configuration.publicKeys();
        assertThat(publicKeyList).hasSize(1).containsExactly("5");

    }

    @Test
    public void fileProvidedUsesValuesFromFileInsteadOfDefault() {

        //fetch this property to reset after test
        final String userHome = System.getProperty("user.home");
        System.setProperty("config.file", "./src/test/resources/other-config-file.yml");
        System.setProperty("user.home", ".");

        final Configuration configuration = ConfigurationFactory.init();

        final List<String> privateKeyList = configuration.privateKeys();
        assertThat(privateKeyList).hasSize(1).containsExactly("other-config-file-value");

        final List<String> publicKeyList = configuration.publicKeys();
        assertThat(publicKeyList).hasSize(1).containsExactly("other-config-file-value");

        System.setProperty("user.home", userHome);
        System.clearProperty("config.file");

    }

}
