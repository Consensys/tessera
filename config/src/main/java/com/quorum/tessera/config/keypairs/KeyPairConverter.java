
package com.quorum.tessera.config.keypairs;

import com.quorum.tessera.encryption.KeyPair;
import com.quorum.tessera.encryption.PrivateKey;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.key.vault.KeyVaultService;

import java.util.Base64;
import java.util.Collection;
import java.util.stream.Collectors;


public class KeyPairConverter {

    private final KeyVaultService keyVaultService;

    public KeyPairConverter(KeyVaultService keyVaultService) {
        this.keyVaultService = keyVaultService;
    }

    public Collection<KeyPair> convert(Collection<ConfigKeyPair> configKeyPairs) {
        return configKeyPairs
                .stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    private KeyPair convert(ConfigKeyPair configKeyPair) {
        String encodedPub;
        String encodedPriv;

        if(configKeyPair instanceof AzureVaultKeyPair) {
            AzureVaultKeyPair akp = (AzureVaultKeyPair) configKeyPair;
            encodedPub = keyVaultService.getSecret(akp.getPublicKeyId());
            encodedPriv = keyVaultService.getSecret(akp.getPrivateKeyId());
        }
        else {
            encodedPub = configKeyPair.getPublicKey();
            encodedPriv = configKeyPair.getPrivateKey();
        }

        return new KeyPair(
            PublicKey.from(Base64.getDecoder().decode(encodedPub)),
            PrivateKey.from(Base64.getDecoder().decode(encodedPriv))
        );
    }
    
}
