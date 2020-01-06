package com.quorum.tessera.config;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class KeyConfigurationTest {

    @Test
    public void loadKeyVaultConfigFromAzureKeyVaultConfig() throws Exception {
        KeyConfiguration keyConfiguration = new KeyConfiguration();
        AzureKeyVaultConfig azureKeyVaultConfig = new AzureKeyVaultConfig();
        keyConfiguration.setAzureKeyVaultConfig(azureKeyVaultConfig);

        KeyVaultConfig result = keyConfiguration.getKeyVaultConfig();
        assertThat(result).isNotNull();


    }

    @Test
    public void loadKeyVaultConfigFromHashicorpKeyVaultConfig() throws Exception {
        KeyConfiguration keyConfiguration = new KeyConfiguration();
        HashicorpKeyVaultConfig hashicorpKeyVaultConfig = new HashicorpKeyVaultConfig();
        keyConfiguration.setHashicorpKeyVaultConfig(hashicorpKeyVaultConfig);

        KeyVaultConfig result = keyConfiguration.getKeyVaultConfig();
        assertThat(result).isNotNull();

    }

    @Test
    public void loadKeyVaultConfigFromNoCOnfg() throws Exception {
        KeyConfiguration keyConfiguration = new KeyConfiguration();

        KeyVaultConfig result = keyConfiguration.getKeyVaultConfig();
        assertThat(result).isNull();

    }


}
