package com.quorum.tessera.config.vault.data;

import com.quorum.tessera.config.KeyVaultType;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AzureGetSecretDataTest {
    @Test
    public void getters() {
        AzureGetSecretData data = new AzureGetSecretData("secretName");

        assertThat(data.getSecretName()).isEqualTo("secretName");
        assertThat(data.getType()).isEqualTo(KeyVaultType.AZURE);
    }
}
