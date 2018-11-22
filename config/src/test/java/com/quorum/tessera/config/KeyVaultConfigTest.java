package com.quorum.tessera.config;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyVaultConfigTest {

    private KeyVaultConfig keyVaultConfig;

    @Before
    public void setUp() {
        this.keyVaultConfig = new KeyVaultConfig(KeyVaultType.AZURE, "someurl");
    }

    @Test
    public void getters() {
        assertThat(keyVaultConfig.getVaultType()).isEqualTo(KeyVaultType.AZURE);
        assertThat(keyVaultConfig.getUrl()).isEqualTo("someurl");
    }

    @Test
    public void setters() {
        keyVaultConfig.setKeyVaultType(null);
        keyVaultConfig.setUrl("newurl");

        assertThat(keyVaultConfig.getVaultType()).isNull();
        assertThat(keyVaultConfig.getUrl()).isEqualTo("newurl");
    }

}
