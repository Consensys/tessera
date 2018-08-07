package com.quorum.tessera.config.keys;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyGeneratorFactoryTest {

    @Test
    public void keyGeneratorIsntNull() {

        final KeyGenerator keyGenerator = KeyGeneratorFactory.newFactory().create();

        assertThat(keyGenerator).isNotNull();

    }


}
