package com.quorum.tessera.key.vault.hashicorp;

import com.bettercloud.vault.VaultConfig;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class VaultConfigFactoryTest {

    @Test
    public void create() {
        VaultConfigFactory vaultConfigFactory = new VaultConfigFactory();
        VaultConfig emptyVaultConfig = new VaultConfig();

        VaultConfig result = vaultConfigFactory.create();

        assertThat(result).isEqualToComparingFieldByField(emptyVaultConfig);
    }
}
