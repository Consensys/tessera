package com.quorum.tessera.key.vault.hashicorp;

import com.bettercloud.vault.VaultConfig;

public class VaultConfigFactory {

    VaultConfig create() {
        return new VaultConfig();
    }
}
