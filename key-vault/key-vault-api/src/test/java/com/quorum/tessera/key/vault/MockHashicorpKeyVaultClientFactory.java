package com.quorum.tessera.key.vault;

import com.quorum.tessera.config.KeyVaultType;

public class MockHashicorpKeyVaultClientFactory implements KeyVaultClientFactory {
    @Override
    public KeyVaultType getType() {
        return KeyVaultType.HASHICORP;
    }
}
