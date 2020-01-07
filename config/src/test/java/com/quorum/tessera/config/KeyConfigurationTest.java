package com.quorum.tessera.config;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class KeyConfigurationTest {

    @Test
    public void loadKeyVaultConfigFromAzureKeyVaultConfig() {
        KeyConfiguration keyConfiguration = new KeyConfiguration();
        AzureKeyVaultConfig azureKeyVaultConfig = new AzureKeyVaultConfig();
        keyConfiguration.setAzureKeyVaultConfig(azureKeyVaultConfig);

        KeyVaultConfig result = keyConfiguration.getKeyVaultConfig();
        assertThat(result).isNotNull();
    }

    @Test
    public void loadKeyVaultConfigFromHashicorpKeyVaultConfig() {
        KeyConfiguration keyConfiguration = new KeyConfiguration();
        HashicorpKeyVaultConfig hashicorpKeyVaultConfig = new HashicorpKeyVaultConfig();
        keyConfiguration.setHashicorpKeyVaultConfig(hashicorpKeyVaultConfig);

        KeyVaultConfig result = keyConfiguration.getKeyVaultConfig();
        assertThat(result).isNotNull();
    }

    @Test
    public void loadKeyVaultConfigFromNoConfg() {
        KeyConfiguration keyConfiguration = new KeyConfiguration();

        KeyVaultConfig result = keyConfiguration.getKeyVaultConfig();
        assertThat(result).isNull();
    }
}
