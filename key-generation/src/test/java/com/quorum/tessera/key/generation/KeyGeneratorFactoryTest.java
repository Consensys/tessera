package com.quorum.tessera.key.generation;

import com.quorum.tessera.config.AWSKeyVaultConfig;
import com.quorum.tessera.config.AzureKeyVaultConfig;
import com.quorum.tessera.config.EncryptorConfig;
import com.quorum.tessera.config.EncryptorType;
import com.quorum.tessera.config.HashicorpKeyVaultConfig;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import java.util.Collections;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KeyGeneratorFactoryTest {

    @Test
    public void fileKeyGeneratorWhenKeyVaultConfigNotProvided() {
        final EnvironmentVariableProvider envProvider = mock(EnvironmentVariableProvider.class);

        EncryptorConfig encryptorConfig = mock(EncryptorConfig.class);
        when(encryptorConfig.getType()).thenReturn(EncryptorType.EC);
        when(encryptorConfig.getProperties()).thenReturn(Collections.EMPTY_MAP);

        final KeyGenerator keyGenerator = KeyGeneratorFactory.newFactory().create(null, encryptorConfig);
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

        final KeyGenerator keyGenerator = KeyGeneratorFactory.newFactory().create(keyVaultConfig, encryptorConfig);

        assertThat(keyGenerator).isNotNull();
        assertThat(keyGenerator).isExactlyInstanceOf(AzureVaultKeyGenerator.class);
    }

    @Test
    public void hashicorpVaultKeyGeneratorWhenHashicorpConfigProvided() {
        final HashicorpKeyVaultConfig keyVaultConfig = new HashicorpKeyVaultConfig();

        EncryptorConfig encryptorConfig = mock(EncryptorConfig.class);
        when(encryptorConfig.getType()).thenReturn(EncryptorType.EC);
        when(encryptorConfig.getProperties()).thenReturn(Collections.EMPTY_MAP);

        final KeyGenerator keyGenerator = KeyGeneratorFactory.newFactory().create(keyVaultConfig, encryptorConfig);

        assertThat(keyGenerator).isNotNull();
        assertThat(keyGenerator).isExactlyInstanceOf(HashicorpVaultKeyGenerator.class);
    }

    @Test
    public void awsVaultKeyGeneratorWhenAwsConfigProvided() {
        final AWSKeyVaultConfig keyVaultConfig = new AWSKeyVaultConfig();

        EncryptorConfig encryptorConfig = mock(EncryptorConfig.class);
        when(encryptorConfig.getType()).thenReturn(EncryptorType.NACL);
        when(encryptorConfig.getProperties()).thenReturn(Collections.EMPTY_MAP);
        final KeyGenerator keyGenerator = KeyGeneratorFactory.newFactory().create(keyVaultConfig, encryptorConfig);

        assertThat(keyGenerator).isNotNull();
        assertThat(keyGenerator).isExactlyInstanceOf(AWSSecretManagerKeyGenerator.class);
    }
}
