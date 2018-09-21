package com.quorum.tessera.config.keys;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyGeneratorFactoryTest {

    @Test
    public void keyGeneratorIsntNullWhenVaultOptionsIsFalse() {
        final KeyGenerator keyGenerator = KeyGeneratorFactory.newFactory().create(false);

        assertThat(keyGenerator).isNotNull();
    }

    @Test
    public void keyGeneratorIsntNullWhenVaultOptionsIsTrue() {
        final KeyGenerator keyGenerator = KeyGeneratorFactory.newFactory().create(true);

        assertThat(keyGenerator).isNotNull();
    }
}
