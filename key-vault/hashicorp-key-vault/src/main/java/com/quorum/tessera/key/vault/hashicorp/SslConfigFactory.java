package com.quorum.tessera.key.vault.hashicorp;

import com.bettercloud.vault.SslConfig;

public class SslConfigFactory {

    public SslConfig create() {
        return new SslConfig();
    }
}
