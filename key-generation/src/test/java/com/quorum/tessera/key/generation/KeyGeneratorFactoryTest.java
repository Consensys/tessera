package com.quorum.tessera.key.generation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import java.util.Collections;
import org.junit.Test;

public class KeyGeneratorFactoryTest {

  @Test
  public void fileKeyGeneratorWhenKeyVaultConfigNotProvided() {
    final EnvironmentVariableProvider envProvider = mock(EnvironmentVariableProvider.class);

    EncryptorConfig encryptorConfig = mock(EncryptorConfig.class);
    when(encryptorConfig.getType()).thenReturn(EncryptorType.EC);
    when(encryptorConfig.getProperties()).thenReturn(Collections.EMPTY_MAP);

    final KeyGenerator keyGenerator =
        KeyGeneratorFactory.newFactory().create(null, encryptorConfig);
    when(envProvider.getEnv(anyString())).thenReturn("env");

    assertThat(keyGenerator).isNotNull();
    assertThat(keyGenerator).isExactlyInstanceOf(FileKeyGenerator.class);
  }

  @Test
  public void azureVaultKeyGeneratorWhenAzureConfigProvided() {
    final AzureKeyVaultConfig keyVaultConfig = new AzureKeyVaultConfig();

    EncryptorConfig encryptorConfig = mock(EncryptorConfig.class);
    when(encryptorConfig.getType()).thenReturn(EncryptorType.EC);
    when(encryptorConfig.getProperties()).thenReturn(Collections.EMPTY_MAP);

    final KeyGenerator keyGenerator =
        KeyGeneratorFactory.newFactory().create(keyVaultConfig, encryptorConfig);

    assertThat(keyGenerator).isNotNull();
    assertThat(keyGenerator).isExactlyInstanceOf(AzureVaultKeyGenerator.class);
  }

  @Test
  public void hashicorpVaultKeyGeneratorWhenHashicorpConfigProvided() {
    final HashicorpKeyVaultConfig keyVaultConfig = new HashicorpKeyVaultConfig();

    EncryptorConfig encryptorConfig = mock(EncryptorConfig.class);
    when(encryptorConfig.getType()).thenReturn(EncryptorType.EC);
    when(encryptorConfig.getProperties()).thenReturn(Collections.EMPTY_MAP);

    final KeyGenerator keyGenerator =
        KeyGeneratorFactory.newFactory().create(keyVaultConfig, encryptorConfig);

    assertThat(keyGenerator).isNotNull();
    assertThat(keyGenerator).isExactlyInstanceOf(HashicorpVaultKeyGenerator.class);
  }

  @Test
  public void awsVaultKeyGeneratorWhenAwsConfigProvided() {
    final DefaultKeyVaultConfig keyVaultConfig = new DefaultKeyVaultConfig();
    keyVaultConfig.setKeyVaultType(KeyVaultType.AWS);

    EncryptorConfig encryptorConfig = mock(EncryptorConfig.class);
    when(encryptorConfig.getType()).thenReturn(EncryptorType.NACL);
    when(encryptorConfig.getProperties()).thenReturn(Collections.EMPTY_MAP);

    final KeyGenerator keyGenerator =
        KeyGeneratorFactory.newFactory().create(keyVaultConfig, encryptorConfig);

    assertThat(keyGenerator).isNotNull();
    assertThat(keyGenerator).isExactlyInstanceOf(AWSSecretManagerKeyGenerator.class);
  }

  @Test
  public void awsVaultKeyGeneratorWhenNonDefaultKeyVaultConfig() {
    final KeyVaultConfig keyVaultConfig = mock(KeyVaultConfig.class);
    when(keyVaultConfig.getKeyVaultType()).thenReturn(KeyVaultType.AWS);

    EncryptorConfig encryptorConfig = mock(EncryptorConfig.class);
    when(encryptorConfig.getType()).thenReturn(EncryptorType.NACL);
    when(encryptorConfig.getProperties()).thenReturn(Collections.EMPTY_MAP);

    final Throwable ex =
        catchThrowable(
            () -> KeyGeneratorFactory.newFactory().create(keyVaultConfig, encryptorConfig));

    assertThat(ex).isInstanceOf(IllegalArgumentException.class);
  }
}
