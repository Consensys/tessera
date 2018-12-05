package com.quorum.tessera.config.vault.data;

import com.quorum.tessera.config.KeyVaultType;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class HashicorpSetSecretDataTest {

    private HashicorpSetSecretData setSecretData;

    @Before
    public void setUp() {
        this.setSecretData = new HashicorpSetSecretData("secret/path", Collections.singletonMap("name", "value"));
    }

    @Test
    public void getters() {
        assertThat(setSecretData.getSecretPath()).isEqualTo("secret/path");
        assertThat(setSecretData.getNameValuePairs()).isEqualTo(Collections.singletonMap("name", "value"));
    }

    @Test
    public void getType() {
        assertThat(setSecretData.getType()).isEqualTo(KeyVaultType.HASHICORP);
    }

}
