package com.quorum.tessera.config.cli;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.util.KeyDataUtil;

public interface KeyDataMarshaller {

    default KeyData marshal(ConfigKeyPair keyPair) {
       return KeyDataUtil.marshal(keyPair);
    }

    static KeyDataMarshaller create() {
        return ServiceLoaderUtil.load(KeyDataMarshaller.class).orElse(new KeyDataMarshaller() {});
    }

}
