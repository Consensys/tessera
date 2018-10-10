
package com.quorum.tessera.config.keypairs;

import com.quorum.tessera.encryption.KeyPair;
import com.quorum.tessera.encryption.PrivateKey;
import com.quorum.tessera.encryption.PublicKey;
import java.util.Base64;
import java.util.Collection;
import java.util.stream.Collectors;


public interface KeyPairConvertor {
    
    static Collection<KeyPair> convert(Collection<ConfigKeyPair> configKeyPairs) {
        return configKeyPairs
                .stream()
                .map(kd
                        -> new KeyPair(
                        PublicKey.from(Base64.getDecoder().decode(kd.getPublicKey())),
                        PrivateKey.from(Base64.getDecoder().decode(kd.getPrivateKey()))
                )
                ).collect(Collectors.toList());
    }
    
}
