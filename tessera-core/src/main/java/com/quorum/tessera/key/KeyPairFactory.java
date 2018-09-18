package com.quorum.tessera.key;

import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.key.vault.KeyVaultService;
import com.quorum.tessera.nacl.Key;
import com.quorum.tessera.nacl.KeyPair;

import java.util.Base64;
import java.util.Objects;

public class KeyPairFactory {

    private KeyVaultService keyVaultService;

    public KeyPairFactory(KeyVaultService keyVaultService) {
        this.keyVaultService = keyVaultService;
    }

    public KeyPair getKeyPair(KeyData keyData) {
        final String privateKey;
        final String publicKey = keyData.getPublicKey();

        if(Objects.isNull(keyData.getPrivateKey())) {
            privateKey = keyVaultService.getSecret(keyData.getAzureKeyVaultId());
        } else {
            privateKey = keyData.getPrivateKey();
        }

        return new KeyPair(
            new Key(Base64.getDecoder().decode(publicKey)),
            new Key(Base64.getDecoder().decode(privateKey))
        );
    }
}
