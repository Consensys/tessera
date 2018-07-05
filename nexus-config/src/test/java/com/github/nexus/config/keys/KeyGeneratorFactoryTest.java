package com.github.nexus.config.keys;

import com.github.nexus.config.keys.KeyGenerator;
import com.github.nexus.config.keys.KeyGeneratorFactory;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyGeneratorFactoryTest {

    @Test
    public void keyGeneratorIsntNull() {

        final KeyGenerator keyGenerator = KeyGeneratorFactory.create();

        assertThat(keyGenerator).isNotNull();

    }


}
