package com.quorum.tessera.config;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HashicorpKeyVaultConfigTest {

    private HashicorpKeyVaultConfig vaultConfig;

    @Before
    public void setUp() {
        vaultConfig = new HashicorpKeyVaultConfig("url");
    }

    @Test
    public void getters() {
        assertThat(vaultConfig.getUrl()).isEqualTo("url");
    }

    @Test
    public void setters() {
        assertThat(vaultConfig.getUrl()).isEqualTo("url");
        vaultConfig.setUrl("newUrl");
        assertThat(vaultConfig.getUrl()).isEqualTo("newUrl");
    }

    @Test
    public void getType() {
        assertThat(vaultConfig.getKeyVaultType()).isEqualTo(KeyVaultType.HASHICORP);
    }

}
