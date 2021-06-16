package com.quorum.tessera.enclave;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.KeyVaultType;
import com.quorum.tessera.config.keypairs.AWSKeyPair;
import com.quorum.tessera.config.keypairs.AzureVaultKeyPair;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.keypairs.HashicorpVaultKeyPair;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.encryption.KeyPair;
import com.quorum.tessera.encryption.PrivateKey;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.key.vault.KeyVaultService;
import com.quorum.tessera.key.vault.KeyVaultServiceFactory;
import java.util.*;
import java.util.stream.Collectors;

public class KeyPairConverter {

  private final Config config;

  private final EnvironmentVariableProvider envProvider;

  public KeyPairConverter(Config config, EnvironmentVariableProvider envProvider) {
    this.config = config;
    this.envProvider = envProvider;
  }

  public Collection<KeyPair> convert(Collection<ConfigKeyPair> configKeyPairs) {
    return configKeyPairs.stream().map(this::convert).collect(Collectors.toList());
  }

  private KeyPair convert(ConfigKeyPair configKeyPair) {
    final String base64PublicKey;
    final String base64PrivateKey;

    if (configKeyPair instanceof AzureVaultKeyPair) {

      KeyVaultServiceFactory keyVaultServiceFactory =
          KeyVaultServiceFactory.getInstance(KeyVaultType.AZURE);

      KeyVaultService keyVaultService = keyVaultServiceFactory.create(config, envProvider);

      AzureVaultKeyPair akp = (AzureVaultKeyPair) configKeyPair;

      Map<String, String> getPublicKeyData =
          new HashMap<>(Map.of("secretName", akp.getPublicKeyId()));
      getPublicKeyData.put("secretVersion", akp.getPublicKeyVersion());

      Map<String, String> getPrivateKeyData =
          new HashMap<>(Map.of("secretName", akp.getPrivateKeyId()));
      getPrivateKeyData.put("secretVersion", akp.getPrivateKeyVersion());

      base64PublicKey = keyVaultService.getSecret(getPublicKeyData);
      base64PrivateKey = keyVaultService.getSecret(getPrivateKeyData);
    } else if (configKeyPair instanceof HashicorpVaultKeyPair) {

      KeyVaultServiceFactory keyVaultServiceFactory =
          KeyVaultServiceFactory.getInstance(KeyVaultType.HASHICORP);

      KeyVaultService keyVaultService = keyVaultServiceFactory.create(config, envProvider);

      HashicorpVaultKeyPair hkp = (HashicorpVaultKeyPair) configKeyPair;

      Map<String, String> getPublicKeyData =
          Map.of(
              "secretEngineName", hkp.getSecretEngineName(),
              "secretName", hkp.getSecretName(),
              "secretId", hkp.getPublicKeyId(),
              "secretVersion", Objects.toString(hkp.getSecretVersion()));

      Map<String, String> getPrivateKeyData =
          Map.of(
              "secretEngineName", hkp.getSecretEngineName(),
              "secretName", hkp.getSecretName(),
              "secretId", hkp.getPrivateKeyId(),
              "secretVersion", Objects.toString(hkp.getSecretVersion()));

      base64PublicKey = keyVaultService.getSecret(getPublicKeyData);
      base64PrivateKey = keyVaultService.getSecret(getPrivateKeyData);
    } else if (configKeyPair instanceof AWSKeyPair) {
      KeyVaultServiceFactory keyVaultServiceFactory =
          KeyVaultServiceFactory.getInstance(KeyVaultType.AWS);

      KeyVaultService keyVaultService = keyVaultServiceFactory.create(config, envProvider);

      AWSKeyPair akp = (AWSKeyPair) configKeyPair;

      Map<String, String> getPublicKeyData = Map.of("secretName", akp.getPublicKeyId());
      Map<String, String> getPrivateKeyData = Map.of("secretName", akp.getPrivateKeyId());

      base64PublicKey = keyVaultService.getSecret(getPublicKeyData);
      base64PrivateKey = keyVaultService.getSecret(getPrivateKeyData);
    } else {

      base64PublicKey = configKeyPair.getPublicKey();
      base64PrivateKey = configKeyPair.getPrivateKey();
    }

    return new KeyPair(
        PublicKey.from(Base64.getDecoder().decode(base64PublicKey.trim())),
        PrivateKey.from(Base64.getDecoder().decode(base64PrivateKey.trim())));
  }

  public List<PublicKey> convert(List<String> values) {
    return Objects.requireNonNull(values, "Key values cannot be null").stream()
        .map(v -> Base64.getDecoder().decode(v))
        .map(PublicKey::from)
        .collect(Collectors.toList());
  }
}
