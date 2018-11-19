
package com.quorum.tessera.keypairconverter;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.keypairs.AzureVaultKeyPair;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.keypairs.KeyPairType;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.encryption.KeyPair;
import com.quorum.tessera.encryption.PrivateKey;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.key.vault.KeyVaultService;
import com.quorum.tessera.key.vault.KeyVaultServiceFactory;

import java.util.Base64;
import java.util.Collection;
import java.util.stream.Collectors;


public class KeyPairConverter {

    private final Config config;

    private final EnvironmentVariableProvider envProvider;

    public KeyPairConverter(Config config, EnvironmentVariableProvider envProvider) {
        this.config = config;
        this.envProvider = envProvider;
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
            KeyVaultServiceFactory keyVaultServiceFactory = KeyVaultServiceFactory.getInstance(KeyPairType.AZURE);
            KeyVaultService keyVaultService = keyVaultServiceFactory.create(config, envProvider);
            //TODO Move KeyVaultService to be a property of ConfigKeyPair, so configKeyPair.getPublicKey() can be used for all key types

            AzureVaultKeyPair akp = (AzureVaultKeyPair) configKeyPair;
            encodedPub = keyVaultService.getSecret(akp.getPublicKeyId());
            encodedPriv = keyVaultService.getSecret(akp.getPrivateKeyId());
        }
        else {
            encodedPub = configKeyPair.getPublicKey();
            encodedPriv = configKeyPair.getPrivateKey();
        }

        return new KeyPair(
            PublicKey.from(Base64.getDecoder().decode(encodedPub.trim())),
            PrivateKey.from(Base64.getDecoder().decode(encodedPriv.trim()))
        );
    }
    
}
