package com.quorum.tessera.key.generation;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyVaultOptionsTest {

    @Test
    public void getters() {
        String secretEngineName = "secretEngineName";

        KeyVaultOptions keyVaultOptions = new KeyVaultOptions(secretEngineName);

        assertThat(keyVaultOptions.getSecretEngineName()).isEqualTo(secretEngineName);
    }
}
