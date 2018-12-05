package com.quorum.tessera.config.vault.data;

import com.quorum.tessera.config.KeyVaultType;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AzureSetSecretDataTest {
    @Test
    public void getters() {
        AzureSetSecretData data = new AzureSetSecretData("secretName", "secret");

        assertThat(data.getSecretName()).isEqualTo("secretName");
        assertThat(data.getSecret()).isEqualTo("secret");
        assertThat(data.getType()).isEqualTo(KeyVaultType.AZURE);
    }
}
