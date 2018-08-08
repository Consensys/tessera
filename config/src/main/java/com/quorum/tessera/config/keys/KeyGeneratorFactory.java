package com.quorum.tessera.config.keys;

import com.quorum.tessera.config.ServiceLoaderUtil;


public interface KeyGeneratorFactory {

    KeyGenerator create();
    
    static KeyGeneratorFactory newFactory() {
        return ServiceLoaderUtil.load(KeyGeneratorFactory.class)
                .orElse(new DefaultKeyGeneratorFactory());
    }

}
