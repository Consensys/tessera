package com.quorum.tessera.key.vault;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.KeyVaultType;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public interface KeyVaultServiceFactory {

    KeyVaultService create(Config config, EnvironmentVariableProvider envProvider);

    KeyVaultType getType();

    static KeyVaultServiceFactory getInstance(KeyVaultType keyVaultType) {
        List<KeyVaultServiceFactory> providers = new ArrayList<>();
        ServiceLoader.load(KeyVaultServiceFactory.class).forEach(providers::add);

        return providers.stream()
                .filter(factory -> factory.getType() == keyVaultType)
                .findFirst()
                .orElseThrow(
                        () ->
                                new NoKeyVaultServiceFactoryException(
                                        keyVaultType
                                                + " implementation of KeyVaultServiceFactory was not found on the classpath"));
    }
}
