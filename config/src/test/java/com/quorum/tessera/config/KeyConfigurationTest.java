package com.quorum.tessera.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.Test;

public class KeyConfigurationTest {

  @Test
  public void loadKeyVaultConfigFromAzureKeyVaultConfig() {
    KeyConfiguration keyConfiguration = new KeyConfiguration();
    AzureKeyVaultConfig azureKeyVaultConfig = new AzureKeyVaultConfig();
    keyConfiguration.addKeyVaultConfig(azureKeyVaultConfig);

    KeyVaultConfig result = keyConfiguration.getKeyVaultConfig(KeyVaultType.AZURE).get();
    assertThat(result).isNotNull();
  }

  @Test
  public void loadKeyVaultConfigFromHashicorpKeyVaultConfig() {
    KeyConfiguration keyConfiguration = new KeyConfiguration();
    HashicorpKeyVaultConfig hashicorpKeyVaultConfig = new HashicorpKeyVaultConfig();
    keyConfiguration.addKeyVaultConfig(hashicorpKeyVaultConfig);

    KeyVaultConfig result = keyConfiguration.getKeyVaultConfig(KeyVaultType.HASHICORP).get();
    assertThat(result).isNotNull();
  }

  @Test
  public void loadBothKeyVaultConfigsFromConstructor() {
    AzureKeyVaultConfig azureKeyVaultConfig = new AzureKeyVaultConfig();
    HashicorpKeyVaultConfig hashicorpKeyVaultConfig = new HashicorpKeyVaultConfig();

    KeyConfiguration keyConfiguration =
        new KeyConfiguration(null, null, null, azureKeyVaultConfig, hashicorpKeyVaultConfig);
    List<KeyVaultConfig> result = keyConfiguration.getKeyVaultConfigs();
    assertThat(result).hasSize(2);
    assertThat(result)
        .flatExtracting(KeyVaultConfig::getKeyVaultType)
        .containsExactlyInAnyOrder(KeyVaultType.AZURE, KeyVaultType.HASHICORP);
  }

  @Test
  public void getKeyVaultConfigsReturnNullIfConfigsAreNull() {
    KeyConfiguration keyConfiguration = new KeyConfiguration(null, null, null, null, null);

    assertThat(keyConfiguration.getKeyVaultConfigs()).isNull();
  }

  @Test
  public void loadKeyVaultConfigFromNoConfig() {
    KeyConfiguration keyConfiguration = new KeyConfiguration();

    Optional<DefaultKeyVaultConfig> result = keyConfiguration.getKeyVaultConfig(KeyVaultType.AZURE);
    assertThat(result).isNotPresent();
  }

  @Test
  public void loadKeyVaultConfigFromDeprecatedTypes() {
    AzureKeyVaultConfig azureKeyVaultConfig = new AzureKeyVaultConfig();
    HashicorpKeyVaultConfig hashicorpKeyVaultConfig = new HashicorpKeyVaultConfig();

    KeyConfiguration keyConfiguration =
        new KeyConfiguration(null, null, null, azureKeyVaultConfig, hashicorpKeyVaultConfig);

    DefaultKeyVaultConfig azureResult =
        keyConfiguration.getKeyVaultConfig(KeyVaultType.AZURE).get();
    assertThat(azureResult).isNotNull();

    DefaultKeyVaultConfig hashicorpResult =
        keyConfiguration.getKeyVaultConfig(KeyVaultType.HASHICORP).get();
    assertThat(hashicorpResult).isNotNull();
  }

  @Test
  public void loadKeyVaultConfigWithNullType() {
    KeyConfiguration keyConfiguration = new KeyConfiguration();

    assertThat(keyConfiguration.getKeyVaultConfig(null)).isNotPresent();
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

  @Test
  public void setPasswordFile() {
    Path file = mock(Path.class);
    KeyConfiguration keyConfiguration = new KeyConfiguration();
    keyConfiguration.setPasswordFile(file);
    assertThat(keyConfiguration.getPasswordFile()).isSameAs(file);
  }
}
