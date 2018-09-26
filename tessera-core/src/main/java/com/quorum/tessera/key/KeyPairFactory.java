package com.quorum.tessera.key;

import com.quorum.tessera.config.keypairs.AzureVaultKeyPair;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.keypairs.ConfigKeyPairType;
import com.quorum.tessera.key.vault.KeyVaultService;
import com.quorum.tessera.nacl.Key;
import com.quorum.tessera.nacl.KeyPair;

import java.util.Base64;

public class KeyPairFactory {

    private KeyVaultService keyVaultService;

    public KeyPairFactory(KeyVaultService keyVaultService) {
        this.keyVaultService = keyVaultService;
    }

    public KeyPair getKeyPair(ConfigKeyPair configKeyPair) {
        final String publicKey;
        final String privateKey;

        if(configKeyPair.getType() == ConfigKeyPairType.AZURE) {
            AzureVaultKeyPair azureVaultKeyPair = (AzureVaultKeyPair) configKeyPair;
            publicKey = keyVaultService.getSecret(azureVaultKeyPair.getPublicKeyId());
            privateKey = keyVaultService.getSecret(azureVaultKeyPair.getPrivateKeyId());
        } else {
            publicKey = configKeyPair.getPublicKey();
            privateKey = configKeyPair.getPrivateKey();
        }

        return new KeyPair(
            new Key(Base64.getDecoder().decode(publicKey)),
            new Key(Base64.getDecoder().decode(privateKey))
        );
    }
}
