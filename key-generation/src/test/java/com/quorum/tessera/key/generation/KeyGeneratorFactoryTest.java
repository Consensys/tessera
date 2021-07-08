package com.quorum.tessera.key.generation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.quorum.tessera.config.DefaultKeyVaultConfig;
import com.quorum.tessera.config.EncryptorConfig;
import com.quorum.tessera.config.EncryptorType;
import com.quorum.tessera.config.KeyVaultType;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.key.vault.KeyVaultService;
import com.quorum.tessera.key.vault.KeyVaultServiceFactory;
import java.security.Security;
import java.util.Collections;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Test;
import org.mockito.MockedStatic;

public class KeyGeneratorFactoryTest {

  static {
    Security.addProvider(new BouncyCastleProvider());
  }

  @Test
  public void fileKeyGeneratorWhenKeyVaultConfigNotProvided() {
    final EnvironmentVariableProvider envProvider = mock(EnvironmentVariableProvider.class);

    EncryptorConfig encryptorConfig = mock(EncryptorConfig.class);
    when(encryptorConfig.getType()).thenReturn(EncryptorType.EC);
    when(encryptorConfig.getProperties()).thenReturn(Collections.EMPTY_MAP);

    final KeyGenerator keyGenerator = KeyGeneratorFactory.create().create(null, encryptorConfig);
    when(envProvider.getEnv(anyString())).thenReturn("env");

    assertThat(keyGenerator).isNotNull();
    assertThat(keyGenerator).isExactlyInstanceOf(FileKeyGenerator.class);
  }

  @Test
  public void awsVaultKeyGeneratorWhenAwsConfigProvided() {

    final DefaultKeyVaultConfig keyVaultConfig = new DefaultKeyVaultConfig();
    keyVaultConfig.setKeyVaultType(KeyVaultType.AWS);

    EncryptorConfig encryptorConfig = mock(EncryptorConfig.class);
    when(encryptorConfig.getType()).thenReturn(EncryptorType.NACL);
    when(encryptorConfig.getProperties()).thenReturn(Collections.EMPTY_MAP);

    KeyGeneratorFactory keyGeneratorFactory = KeyGeneratorFactory.create();

    try (MockedStatic<KeyVaultServiceFactory> mockedKeyVaultServiceFactory =
        mockStatic(KeyVaultServiceFactory.class)) {

      KeyVaultService keyVaultService = mock(KeyVaultService.class);
      KeyVaultServiceFactory keyVaultServiceFactory = mock(KeyVaultServiceFactory.class);
      when(keyVaultServiceFactory.create(any(), any())).thenReturn(keyVaultService);

      mockedKeyVaultServiceFactory
          .when(() -> KeyVaultServiceFactory.getInstance(KeyVaultType.AWS))
          .thenReturn(keyVaultServiceFactory);

      final KeyGenerator keyGenerator = keyGeneratorFactory.create(keyVaultConfig, encryptorConfig);

      assertThat(keyGenerator).isNotNull();
      assertThat(keyGenerator).isExactlyInstanceOf(AWSSecretManagerKeyGenerator.class);
    }
  }
}
