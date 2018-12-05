package com.quorum.tessera.key.vault.hashicorp;

import com.bettercloud.vault.SslConfig;

//Ensures a newly instantiated SslConfig object is used in the HashicorpKeyVaultClientFactory
public class SslConfigFactory {

    public SslConfig create() {
        return new SslConfig();
    }
}
