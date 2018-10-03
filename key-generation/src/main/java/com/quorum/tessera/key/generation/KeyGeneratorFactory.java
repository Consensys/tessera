package com.quorum.tessera.key.generation;

import com.quorum.tessera.ServiceLoaderUtil;


public interface KeyGeneratorFactory {

    KeyGenerator create();
    
    static KeyGeneratorFactory newFactory() {
        return ServiceLoaderUtil.load(KeyGeneratorFactory.class).orElse(new DefaultKeyGeneratorFactory());
    }

}
