package com.quorum.tessera.config.keys;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyEncryptorFactoryTest {

    @Test
    public void keyEncryptorIsntNull() {

        final KeyEncryptor keyEncryptor = KeyEncryptorFactory.create();

        assertThat(keyEncryptor).isNotNull();

    }

}
