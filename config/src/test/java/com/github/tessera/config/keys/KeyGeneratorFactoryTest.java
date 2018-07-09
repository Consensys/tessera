package com.github.tessera.config.keys;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyGeneratorFactoryTest {

    @Test
    public void keyGeneratorIsntNull() {

        final KeyGenerator keyGenerator = KeyGeneratorFactory.create();

        assertThat(keyGenerator).isNotNull();

    }


}
