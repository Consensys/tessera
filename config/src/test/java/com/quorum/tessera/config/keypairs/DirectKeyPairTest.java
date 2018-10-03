package com.quorum.tessera.config.keypairs;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DirectKeyPairTest {

    @Test
    public void settingPasswordDoesntDoAnything() {

        final DirectKeyPair keyPair = new DirectKeyPair("public", "private");

        keyPair.withPassword("randomPassword");

        assertThat(keyPair.getPassword()).isEqualTo("");

    }

    @Test
    public void getters() {
        final DirectKeyPair keyPair = new DirectKeyPair("public", "private");

        assertThat(keyPair.getPublicKey()).isEqualTo("public");
        assertThat(keyPair.getPrivateKey()).isEqualTo("private");
    }

}
