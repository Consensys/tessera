package com.quorum.tessera.config;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HashicorpKeyVaultConfigTest {

    private HashicorpKeyVaultConfig vaultConfig;

    @Before
    public void setUp() {
        vaultConfig = new HashicorpKeyVaultConfig();
        vaultConfig.setUrl("url");
    }

    @Test
    public void getType() {
        assertThat(vaultConfig.getKeyVaultType()).isEqualTo(KeyVaultType.HASHICORP);
    }

    @Test
    public void getApprolePathReturnsDefaultIfNotSet() {
        assertThat(vaultConfig.getApprolePath()).isEqualTo("approle");
    }

    @Test
    public void getApprolePath() {
        vaultConfig.setApprolePath("notdefault");
        assertThat(vaultConfig.getApprolePath()).isEqualTo("notdefault");
    }

}
