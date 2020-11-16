package com.quorum.tessera.enclave;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.KeyVaultType;
import com.quorum.tessera.config.keypairs.AWSKeyPair;
import com.quorum.tessera.config.keypairs.AzureVaultKeyPair;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.keypairs.HashicorpVaultKeyPair;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.config.vault.data.AWSGetSecretData;
import com.quorum.tessera.config.vault.data.AzureGetSecretData;
import com.quorum.tessera.config.vault.data.GetSecretData;
import com.quorum.tessera.config.vault.data.HashicorpGetSecretData;
import com.quorum.tessera.encryption.KeyPair;
import com.quorum.tessera.encryption.PrivateKey;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.key.vault.KeyVaultService;
import com.quorum.tessera.key.vault.KeyVaultServiceFactory;

import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
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
        final String base64PublicKey;
        final String base64PrivateKey;

        if (configKeyPair instanceof AzureVaultKeyPair) {

            KeyVaultServiceFactory keyVaultServiceFactory = KeyVaultServiceFactory.getInstance(KeyVaultType.AZURE);

            KeyVaultService keyVaultService = keyVaultServiceFactory.create(config, envProvider);

            AzureVaultKeyPair akp = (AzureVaultKeyPair) configKeyPair;

            GetSecretData getPublicKeyData = new AzureGetSecretData(akp.getPublicKeyId(), akp.getPublicKeyVersion());
            GetSecretData getPrivateKeyData = new AzureGetSecretData(akp.getPrivateKeyId(), akp.getPrivateKeyVersion());

            base64PublicKey = keyVaultService.getSecret(getPublicKeyData);
            base64PrivateKey = keyVaultService.getSecret(getPrivateKeyData);
        } else if (configKeyPair instanceof HashicorpVaultKeyPair) {

            KeyVaultServiceFactory keyVaultServiceFactory = KeyVaultServiceFactory.getInstance(KeyVaultType.HASHICORP);

            KeyVaultService keyVaultService = keyVaultServiceFactory.create(config, envProvider);

            HashicorpVaultKeyPair hkp = (HashicorpVaultKeyPair) configKeyPair;

            GetSecretData getPublicKeyData = new HashicorpGetSecretData(hkp.getSecretEngineName(), hkp.getSecretName(), hkp.getPublicKeyId(), hkp.getSecretVersion());
            GetSecretData getPrivateKeyData = new HashicorpGetSecretData(hkp.getSecretEngineName(), hkp.getSecretName(), hkp.getPrivateKeyId(), hkp.getSecretVersion());

            base64PublicKey = keyVaultService.getSecret(getPublicKeyData);
            base64PrivateKey = keyVaultService.getSecret(getPrivateKeyData);
        } else if (configKeyPair instanceof AWSKeyPair) {
            KeyVaultServiceFactory keyVaultServiceFactory = KeyVaultServiceFactory.getInstance(KeyVaultType.AWS);

            KeyVaultService keyVaultService = keyVaultServiceFactory.create(config, envProvider);

            AWSKeyPair akp = (AWSKeyPair) configKeyPair;

            GetSecretData getPublicKeyData = new AWSGetSecretData(akp.getPublicKeyId());
            GetSecretData getPrivateKeyData = new AWSGetSecretData(akp.getPrivateKeyId());

            base64PublicKey = keyVaultService.getSecret(getPublicKeyData);
            base64PrivateKey = keyVaultService.getSecret(getPrivateKeyData);
        } else {

            base64PublicKey = configKeyPair.getPublicKey();
            base64PrivateKey = configKeyPair.getPrivateKey();

        }

        return new KeyPair(
            PublicKey.from(Base64.getDecoder().decode(base64PublicKey.trim())),
            PrivateKey.from(Base64.getDecoder().decode(base64PrivateKey.trim()))
        );
    }

    public List<PublicKey> convert(List<String> values) {
        return Objects.requireNonNull(values, "Key values cannot be null")
            .stream()
            .map(v -> Base64.getDecoder().decode(v))
            .map(PublicKey::from)
            .collect(Collectors.toList());
    }

}
