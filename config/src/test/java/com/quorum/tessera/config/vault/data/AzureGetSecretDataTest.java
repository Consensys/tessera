package com.quorum.tessera.config.vault.data;

import com.quorum.tessera.config.KeyVaultType;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AzureGetSecretDataTest {
    @Test
    public void getters() {
        AzureGetSecretData data = new AzureGetSecretData("name", "version");

        assertThat(data.getSecretName()).isEqualTo("name");
        assertThat(data.getSecretVersion()).isEqualTo("version");
        assertThat(data.getType()).isEqualTo(KeyVaultType.AZURE);
    }
}
