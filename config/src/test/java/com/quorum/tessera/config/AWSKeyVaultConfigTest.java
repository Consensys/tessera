package com.quorum.tessera.config;

import org.junit.Before;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class AWSKeyVaultConfigTest {

    private AWSKeyVaultConfig keyVaultConfig;

    @Before
    public void setUp() {
        this.keyVaultConfig = new AWSKeyVaultConfig("someendpoint");
    }

    @Test
    public void getters() {
        assertThat(keyVaultConfig.getEndpoint()).isEqualTo("someendpoint");
    }

    @Test
    public void setters() {
        keyVaultConfig.setEndpoint("newendpoint");

        assertThat(keyVaultConfig.getEndpoint()).isEqualTo("newendpoint");
    }

    @Test
    public void getKeyVaultType() {
        assertThat(keyVaultConfig.getKeyVaultType()).isEqualTo(KeyVaultType.AWS);
    }
}
