package com.quorum.tessera.config.keypairs;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DirectKeyPairTest {

    private DirectKeyPair keyPair;

    @Before
    public void setUp() {
        keyPair = new DirectKeyPair("public", "private");
    }

    @Test
    public void settingPasswordDoesntDoAnything() {
        keyPair.withPassword("randomPassword".toCharArray());

        assertThat(keyPair.getPassword()).isEqualTo("");

    }

    @Test
    public void getters() {
        keyPair = new DirectKeyPair("public", "private");

        assertThat(keyPair.getPublicKey()).isEqualTo("public");
        assertThat(keyPair.getPrivateKey()).isEqualTo("private");
    }
}
