package com.quorum.tessera.key.generation;

import com.quorum.tessera.config.ArgonOptions;
import com.quorum.tessera.config.keypairs.HashicorpVaultKeyPair;
import com.quorum.tessera.config.vault.data.HashicorpGetSecretData;
import com.quorum.tessera.config.vault.data.HashicorpSetSecretData;
import com.quorum.tessera.encryption.Encryptor;
import com.quorum.tessera.encryption.KeyPair;
import com.quorum.tessera.key.vault.KeyVaultService;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HashicorpVaultKeyGenerator implements KeyGenerator {

  private static final Logger LOGGER = LoggerFactory.getLogger(HashicorpVaultKeyGenerator.class);

  private final Encryptor nacl;

  private final KeyVaultService<HashicorpSetSecretData, HashicorpGetSecretData> keyVaultService;

  public HashicorpVaultKeyGenerator(
      final Encryptor nacl,
      KeyVaultService<HashicorpSetSecretData, HashicorpGetSecretData> keyVaultService) {
    this.nacl = nacl;
    this.keyVaultService = keyVaultService;
  }

  @Override
  public HashicorpVaultKeyPair generate(
      String filename, ArgonOptions encryptionOptions, KeyVaultOptions keyVaultOptions) {
    Objects.requireNonNull(filename);
    Objects.requireNonNull(
        keyVaultOptions,
        "-keygenvaultsecretengine must be provided if using the Hashicorp vault type");
    Objects.requireNonNull(
        keyVaultOptions.getSecretEngineName(),
        "-keygenvaultsecretengine must be provided if using the Hashicorp vault type");

    final KeyPair keys = this.nacl.generateNewKeys();

    String pubId = "publicKey";
    String privId = "privateKey";
    Map<String, Object> keyPairData = new HashMap<>();
    keyPairData.put(pubId, keys.getPublicKey().encodeToBase64());
    keyPairData.put(privId, keys.getPrivateKey().encodeToBase64());

    keyVaultService.setSecret(
        new HashicorpSetSecretData(keyVaultOptions.getSecretEngineName(), filename, keyPairData));
    LOGGER.debug(
        "Key {} saved to vault secret engine {} with name {} and id {}",
        keyPairData.get(pubId),
        keyVaultOptions.getSecretEngineName(),
        filename,
        pubId);
    LOGGER.info(
        "Key saved to vault secret engine {} with name {} and id {}",
        keyVaultOptions.getSecretEngineName(),
        filename,
        pubId);
    LOGGER.debug(
        "Key {} saved to vault secret engine {} with name {} and id {}",
        keyPairData.get(privId),
        keyVaultOptions.getSecretEngineName(),
        filename,
        privId);
    LOGGER.info(
        "Key saved to vault secret engine {} with name {} and id {}",
        keyVaultOptions.getSecretEngineName(),
        filename,
        privId);

    return new HashicorpVaultKeyPair(
        pubId, privId, keyVaultOptions.getSecretEngineName(), filename, null);
  }
}
