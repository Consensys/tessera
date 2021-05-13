package com.quorum.tessera.key.vault;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.KeyVaultType;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;

public interface KeyVaultServiceFactory {

  KeyVaultService create(Config config, EnvironmentVariableProvider envProvider);

  KeyVaultType getType();

  static KeyVaultServiceFactory getInstance(KeyVaultType keyVaultType) {
    return ServiceLoaderUtil.loadAll(KeyVaultServiceFactory.class)
        .filter(factory -> factory.getType() == keyVaultType)
        .findFirst()
        .orElseThrow(
            () ->
                new NoKeyVaultServiceFactoryException(
                    keyVaultType
                        + " implementation of KeyVaultServiceFactory was not found on the classpath"));
  }
}
