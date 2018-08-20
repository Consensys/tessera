package com.quorum.tessera.config.keys;

import com.quorum.tessera.ServiceLoaderUtil;


public interface KeyGeneratorFactory {

    KeyGenerator create();
    
    static KeyGeneratorFactory newFactory() {
        return ServiceLoaderUtil.load(KeyGeneratorFactory.class).orElse(new DefaultKeyGeneratorFactory());
    }

}
