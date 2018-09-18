package com.quorum.tessera.config.keys;

import static org.mockito.Mockito.mock;

public class MockKeyGeneratorFactory implements KeyGeneratorFactory {

    public enum KeyGeneratorHolder {
        INSTANCE;

        KeyGenerator keyGenerator = mock(KeyGenerator.class);

    }

    @Override
    public KeyGenerator create() {
        return getMockKeyGenerator();
    }

    public static KeyGenerator getMockKeyGenerator() {
        return KeyGeneratorHolder.INSTANCE.keyGenerator;
    }

    public static void reset() {
        KeyGeneratorHolder.INSTANCE.keyGenerator =  mock(KeyGenerator.class);
    }

}
