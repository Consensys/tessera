package com.quorum.tessera.key.generation;

import com.quorum.tessera.config.ArgonOptions;
import com.quorum.tessera.config.keypairs.AWSKeyPair;
import com.quorum.tessera.encryption.Encryptor;
import com.quorum.tessera.encryption.Key;
import com.quorum.tessera.encryption.KeyPair;
import com.quorum.tessera.key.vault.KeyVaultService;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AWSSecretManagerKeyGenerator implements KeyGenerator {
  private static final Logger LOGGER = LoggerFactory.getLogger(AWSSecretManagerKeyGenerator.class);

  private final Encryptor encryptor;
  private final KeyVaultService keyVaultService;

  public AWSSecretManagerKeyGenerator(Encryptor encryptor, KeyVaultService keyVaultService) {

    this.encryptor = Objects.requireNonNull(encryptor);
    this.keyVaultService = Objects.requireNonNull(keyVaultService);
  }

  @Override
  public AWSKeyPair generate(
      String filename, ArgonOptions encryptionOptions, KeyVaultOptions keyVaultOptions) {
    final KeyPair keys = this.encryptor.generateNewKeys();

    final StringBuilder publicId = new StringBuilder();
    final StringBuilder privateId = new StringBuilder();

    if (filename != null) {
      final Path path = Paths.get(filename);
      final String secretId = path.getFileName().toString();

      if (!secretId.matches("^[0-9a-zA-Z\\-/_+=.@]*$")) {
        throw new UnsupportedCharsetException(
            "Generated key ID for AWS Secret Manager can contain only 0-9, a-z, A-Z and /_+=.@- characters");
      }

      publicId.append(secretId);
      privateId.append(secretId);
    }

    publicId.append("Pub");
    privateId.append("Key");

    saveKeyInSecretManager(publicId.toString(), keys.getPublicKey());
    saveKeyInSecretManager(privateId.toString(), keys.getPrivateKey());

    return new AWSKeyPair(publicId.toString(), privateId.toString());
  }

  private void saveKeyInSecretManager(String id, Key key) {
    keyVaultService.setSecret(Map.of("secretName", id, "secret", key.encodeToBase64()));
    LOGGER.debug("Key {} saved to vault with id {}", key.encodeToBase64(), id);
    LOGGER.info("Key saved to vault with id {}", id);
  }
}
