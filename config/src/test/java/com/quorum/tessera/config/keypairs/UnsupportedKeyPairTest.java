package com.quorum.tessera.config.keypairs;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UnsupportedKeyPairTest {

    private UnsupportedKeyPair keyPair;

    @Before
    public void setUp() {
        this.keyPair = new UnsupportedKeyPair(null, null, null, null, null, null, null, null, null, null, null, null);
    }

    @Test
    public void getPasswordAlwaysReturnsNull() {
        assertThat(keyPair.getPassword()).isNull();

        keyPair.withPassword("password");

        assertThat(keyPair.getPassword()).isNull();
    }

    @Test
    public void setHashicorpVaultSecretVersion() {
        assertThat(keyPair.getHashicorpVaultSecretVersion()).isNull();

        keyPair.setHashicorpVaultSecretVersion("1");

        assertThat(keyPair.getHashicorpVaultSecretVersion()).isEqualTo("1");
    }

}
