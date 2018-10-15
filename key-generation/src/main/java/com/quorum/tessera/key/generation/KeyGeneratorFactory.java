package com.quorum.tessera.key.generation;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.config.KeyVaultConfig;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;


public interface KeyGeneratorFactory {

    KeyGenerator create(KeyVaultConfig keyVaultConfig, EnvironmentVariableProvider envProvider);

    static KeyGeneratorFactory newFactory() {
        return ServiceLoaderUtil.load(KeyGeneratorFactory.class).orElse(new DefaultKeyGeneratorFactory());
    }

}
