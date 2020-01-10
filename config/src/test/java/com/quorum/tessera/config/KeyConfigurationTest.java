package com.quorum.tessera.config;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyConfigurationTest {

    @Test
    public void loadKeyVaultConfigFromAzureKeyVaultConfig() {
        KeyConfiguration keyConfiguration = new KeyConfiguration();
        AzureKeyVaultConfig azureKeyVaultConfig = new AzureKeyVaultConfig();
        keyConfiguration.addKeyVaultConfig(azureKeyVaultConfig);

        KeyVaultConfig result = keyConfiguration.getKeyVaultConfig(KeyVaultType.AZURE);
        assertThat(result).isNotNull();
    }

    @Test
    public void loadKeyVaultConfigFromHashicorpKeyVaultConfig() {
        KeyConfiguration keyConfiguration = new KeyConfiguration();
        HashicorpKeyVaultConfig hashicorpKeyVaultConfig = new HashicorpKeyVaultConfig();
        keyConfiguration.addKeyVaultConfig(hashicorpKeyVaultConfig);

        KeyVaultConfig result = keyConfiguration.getKeyVaultConfig(KeyVaultType.HASHICORP);
        assertThat(result).isNotNull();
    }

    @Test
    public void loadKeyVaultConfigFromNoConfig() {
        KeyConfiguration keyConfiguration = new KeyConfiguration();

        KeyVaultConfig result = keyConfiguration.getKeyVaultConfig(KeyVaultType.AZURE);
        assertThat(result).isNull();
    }

    @Test
    public void addMultipleKeyVaultConfigs() {
        KeyConfiguration keyConfiguration = new KeyConfiguration();

        AzureKeyVaultConfig azure = new AzureKeyVaultConfig();
        HashicorpKeyVaultConfig hashicorp = new HashicorpKeyVaultConfig();
        DefaultKeyVaultConfig aws = new DefaultKeyVaultConfig();
        aws.setKeyVaultType(KeyVaultType.AWS);

        keyConfiguration.addKeyVaultConfig(azure);
        keyConfiguration.addKeyVaultConfig(hashicorp);
        keyConfiguration.addKeyVaultConfig(aws);

        DefaultKeyVaultConfig convertedAzure = new DefaultKeyVaultConfig();
        convertedAzure.setKeyVaultType(KeyVaultType.AZURE);
        convertedAzure.setProperty("url", null);

        DefaultKeyVaultConfig convertedHashicorp = new DefaultKeyVaultConfig();
        convertedHashicorp.setKeyVaultType(KeyVaultType.HASHICORP);
        convertedHashicorp.setProperty("url", null);
        convertedHashicorp.setProperty("approlePath", "approle");

        assertThat(keyConfiguration.getKeyVaultConfigs())
                .containsExactlyInAnyOrder(convertedAzure, convertedHashicorp, aws);
    }
}
