package com.quorum.tessera.config.vault.data;

import com.quorum.tessera.config.KeyVaultType;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HashicorpGetSecretDataTest {

    private HashicorpGetSecretData getSecretData;

    @Before
    public void setUp() {
        this.getSecretData = new HashicorpGetSecretData("secret/path", "secretName");
    }

    @Test
    public void getters() {
        assertThat(getSecretData.getSecretPath()).isEqualTo("secret/path");
        assertThat(getSecretData.getSecretName()).isEqualTo("secretName");
    }

    @Test
    public void getType() {
        assertThat(getSecretData.getType()).isEqualTo(KeyVaultType.HASHICORP);
    }

}
