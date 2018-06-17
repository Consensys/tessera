package com.github.nexus.keygen;

import com.github.nexus.configuration.Configuration;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class KeyGeneratorFactoryTest {

    @Test
    public void keyGeneratorIsntNull() {

        final KeyGenerator keyGenerator = KeyGeneratorFactory.create(mock(Configuration.class));

        assertThat(keyGenerator).isNotNull();

    }


}
