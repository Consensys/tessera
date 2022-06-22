package com.quorum.tessera.key.generation;

import com.quorum.tessera.config.ArgonOptions;
import com.quorum.tessera.config.keypairs.HashicorpVaultKeyPair;
import com.quorum.tessera.encryption.Encryptor;
import com.quorum.tessera.encryption.KeyPair;
import com.quorum.tessera.key.vault.KeyVaultService;
import com.quorum.tessera.key.vault.SetSecretResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HashicorpVaultKeyGenerator implements KeyGenerator {

  private static final Logger LOGGER = LoggerFactory.getLogger(HashicorpVaultKeyGenerator.class);

  private final Encryptor encryptor;

  private final KeyVaultService keyVaultService;

  public HashicorpVaultKeyGenerator(final Encryptor encryptor, KeyVaultService keyVaultService) {
    this.encryptor = Objects.requireNonNull(encryptor);
    this.keyVaultService = keyVaultService;
  }

  @Override
  public GeneratedKeyPair generate(
      String filename, ArgonOptions encryptionOptions, KeyVaultOptions keyVaultOptions) {
    Objects.requireNonNull(filename);
    Objects.requireNonNull(
        keyVaultOptions,
        "-keygenvaultsecretengine must be provided if using the Hashicorp vault type");
    Objects.requireNonNull(
        keyVaultOptions.getSecretEngineName(),
        "-keygenvaultsecretengine must be provided if using the Hashicorp vault type");

    final KeyPair keys = this.encryptor.generateNewKeys();

    String pubId = "publicKey";
    String privId = "privateKey";

    Map<String, String> setSecretData = new HashMap<>();
    setSecretData.put(pubId, keys.getPublicKey().encodeToBase64());
    setSecretData.put(privId, keys.getPrivateKey().encodeToBase64());
    setSecretData.put("secretName", filename);
    setSecretData.put("secretEngineName", keyVaultOptions.getSecretEngineName());

    SetSecretResponse resp = keyVaultService.setSecret(setSecretData);
    Integer version =
        Optional.ofNullable(resp)
            .map(r -> r.getProperty("version"))
            .map(Integer::valueOf)
            .orElse(0);

    LOGGER.debug(
        "Key saved to vault secret engine {} with name {} and id {}",
        keyVaultOptions.getSecretEngineName(),
        filename,
        pubId);

    LOGGER.debug(
        "Key saved to vault secret engine {} with name {} and id {}",
        keyVaultOptions.getSecretEngineName(),
        filename,
        privId);

    HashicorpVaultKeyPair keyPair =
        new HashicorpVaultKeyPair(
            pubId, privId, keyVaultOptions.getSecretEngineName(), filename, version);

    return new GeneratedKeyPair(keyPair, keys.getPublicKey().encodeToBase64());
  }
}
