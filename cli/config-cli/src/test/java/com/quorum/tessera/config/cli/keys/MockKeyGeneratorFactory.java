package com.quorum.tessera.config.cli.keys;

import com.quorum.tessera.config.EncryptorConfig;
import com.quorum.tessera.config.KeyVaultConfig;
import com.quorum.tessera.key.generation.KeyGenerator;
import com.quorum.tessera.key.generation.KeyGeneratorFactory;

import static org.mockito.Mockito.mock;

public class MockKeyGeneratorFactory implements KeyGeneratorFactory {

    public enum KeyGeneratorHolder {
        INSTANCE;

        KeyGenerator keyGenerator = mock(KeyGenerator.class);
    }

    @Override
    public KeyGenerator create(KeyVaultConfig keyVaultConfig, EncryptorConfig encryptorConfig) {
        return getMockKeyGenerator();
    }

    public static KeyGenerator getMockKeyGenerator() {
        return KeyGeneratorHolder.INSTANCE.keyGenerator;
    }

    public static void reset() {
        KeyGeneratorHolder.INSTANCE.keyGenerator = mock(KeyGenerator.class);
    }
}
