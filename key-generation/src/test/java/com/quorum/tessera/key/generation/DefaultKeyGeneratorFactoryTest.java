package com.quorum.tessera.key.generation;

import com.quorum.tessera.config.*;
import com.quorum.tessera.key.vault.KeyVaultServiceFactory;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.*;

public class DefaultKeyGeneratorFactoryTest {

    private Map<KeyVaultType,Class<? extends KeyVaultServiceFactory>> lookup =
        Map.of(
            KeyVaultType.AWS,MockAwsVaultServiceFactory.class,
            KeyVaultType.AZURE,MockAzureKeyVaultServiceFactory.class,
            KeyVaultType.HASHICORP,MockHashicorpKeyVaultServiceFactory.class
        );

    private Map<KeyVaultType,Class<? extends KeyGenerator>> resultsLookup =
        Map.of(
            KeyVaultType.AWS,AWSSecretManagerKeyGenerator.class,
            KeyVaultType.AZURE,AzureVaultKeyGenerator.class,
            KeyVaultType.HASHICORP,HashicorpVaultKeyGenerator.class
        );

    @Test
    public void createKeyGeneratorsFromTypes() throws Exception {

        for (KeyVaultType keyVaultType : KeyVaultType.values()) {

            DefaultKeyVaultConfig keyVaultConfig = mock(DefaultKeyVaultConfig.class);
            when(keyVaultConfig.getKeyVaultType()).thenReturn(keyVaultType);
            EncryptorConfig encryptorConfig = mock(EncryptorConfig.class);
            when(encryptorConfig.getType()).thenReturn(EncryptorType.NACL);

            DefaultKeyGeneratorFactory defaultKeyGeneratorFactory = new DefaultKeyGeneratorFactory();
            try (MockedStatic<KeyVaultServiceFactory> mockedKeyVaultServiceFactory = mockStatic(KeyVaultServiceFactory.class)) {
                mockedKeyVaultServiceFactory.when(() -> KeyVaultServiceFactory.getInstance(keyVaultType))
                    .thenReturn(lookup.get(keyVaultType).getConstructor().newInstance());

                final KeyGenerator keyGenerator = defaultKeyGeneratorFactory.create(keyVaultConfig, encryptorConfig);

                assertThat(keyGenerator).isNotNull();
                assertThat(keyGenerator).isExactlyInstanceOf(resultsLookup.get(keyVaultType));
            }
        }
    }

    @Test
    public void awsRequiresThatKeyConfigIsOfTypeDefaultKeyVaultConfig() {
        KeyVaultConfig keyVaultConfig = mock(KeyVaultConfig.class);
        when(keyVaultConfig.getKeyVaultType()).thenReturn(KeyVaultType.AWS);
        EncryptorConfig encryptorConfig = mock(EncryptorConfig.class);
        when(encryptorConfig.getType()).thenReturn(EncryptorType.NACL);

        DefaultKeyGeneratorFactory defaultKeyGeneratorFactory = new DefaultKeyGeneratorFactory();

        try (MockedStatic<KeyVaultServiceFactory> mockedKeyVaultServiceFactory = mockStatic(KeyVaultServiceFactory.class)) {
            mockedKeyVaultServiceFactory.when(() -> KeyVaultServiceFactory.getInstance(KeyVaultType.AWS))
                .thenReturn(new MockAwsVaultServiceFactory());

            try {
                defaultKeyGeneratorFactory.create(keyVaultConfig, encryptorConfig);
                failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
            } catch (IllegalArgumentException ex) {
                assertThat(ex).hasMessage("AWS key vault config not instance of DefaultKeyVaultConfig");
            }
        }

    }

}
