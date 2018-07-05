package com.github.nexus.config.keys;

import com.github.nexus.config.keys.KeyEncryptor;
import com.github.nexus.config.keys.KeyEncryptorFactory;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyEncryptorFactoryTest {

    @Test
    public void keyEncryptorIsntNull() {

        final KeyEncryptor keyEncryptor = KeyEncryptorFactory.create();

        assertThat(keyEncryptor).isNotNull();

    }

}
