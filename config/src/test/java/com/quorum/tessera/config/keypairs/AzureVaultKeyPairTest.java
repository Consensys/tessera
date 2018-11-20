package com.quorum.tessera.config.keypairs;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AzureVaultKeyPairTest {

    private AzureVaultKeyPair keyPair;

    @Before
    public void setUp() {
        keyPair = new AzureVaultKeyPair("pubId", "privId");
    }

    @Test
    public void getPrivateAndPublicKeysReturnsNull() {
        assertThat(keyPair.getPublicKey()).isNull();
        assertThat(keyPair.getPrivateKey()).isNull();
    }

    @Test
    public void getIdsReturnsConstructorSetValues() {
        assertThat(keyPair.getPublicKeyId()).isEqualTo("pubId");
        assertThat(keyPair.getPrivateKeyId()).isEqualTo("privId");
    }

    @Test
    public void getPasswordAlwaysReturnsEmptyString() {
        assertThat(keyPair.getPassword()).isEmpty();

        keyPair.withPassword("password");

        assertThat(keyPair.getPassword()).isEmpty();
    }

    @Test
    public void getType() {
        assertThat(keyPair.getType()).isEqualTo(KeyPairType.AZURE);
    }
}
