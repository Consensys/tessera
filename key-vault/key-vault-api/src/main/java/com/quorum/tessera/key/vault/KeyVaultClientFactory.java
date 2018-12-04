package com.quorum.tessera.key.vault;

import com.quorum.tessera.config.KeyVaultType;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public interface KeyVaultClientFactory {
    KeyVaultType getType();

    static KeyVaultClientFactory getInstance(KeyVaultType keyVaultType) {
        List<KeyVaultClientFactory> providers = new ArrayList<>();
        ServiceLoader.load(KeyVaultClientFactory.class).forEach(providers::add);

        return providers.stream()
                        .filter(factory -> factory.getType() == keyVaultType)
                        .findFirst()
                        .orElse(null);
    }

}
