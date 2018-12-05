package com.quorum.tessera.key.vault.hashicorp;

import com.bettercloud.vault.VaultConfig;

//Ensures a newly instantiated VaultConfig object is used in the HashicorpKeyVaultClientFactory
public class VaultConfigFactory {

    VaultConfig create() {
        return new VaultConfig();
    }
}
