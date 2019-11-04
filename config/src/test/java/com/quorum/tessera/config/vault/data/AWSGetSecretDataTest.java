package com.quorum.tessera.config.vault.data;

import com.quorum.tessera.config.KeyVaultType;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class AWSGetSecretDataTest {
    @Test
    public void getters() {
        AWSGetSecretData data = new AWSGetSecretData("name");

        assertThat(data.getSecretName()).isEqualTo("name");
        assertThat(data.getType()).isEqualTo(KeyVaultType.AWS);
    }
}
