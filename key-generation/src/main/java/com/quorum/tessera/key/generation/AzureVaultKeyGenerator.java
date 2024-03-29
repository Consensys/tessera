package com.quorum.tessera.key.generation;

import com.quorum.tessera.config.ArgonOptions;
import com.quorum.tessera.config.keypairs.AzureVaultKeyPair;
import com.quorum.tessera.encryption.Encryptor;
import com.quorum.tessera.encryption.Key;
import com.quorum.tessera.encryption.KeyPair;
import com.quorum.tessera.key.vault.KeyVaultService;
import com.quorum.tessera.key.vault.SetSecretResponse;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AzureVaultKeyGenerator implements KeyGenerator {

  private static final Logger LOGGER = LoggerFactory.getLogger(AzureVaultKeyGenerator.class);

  private final Encryptor nacl;

  private final KeyVaultService keyVaultService;

  public AzureVaultKeyGenerator(final Encryptor nacl, KeyVaultService keyVaultService) {
    this.nacl = nacl;
    this.keyVaultService = keyVaultService;
  }

  @Override
  public GeneratedKeyPair generate(
      String filename, ArgonOptions encryptionOptions, KeyVaultOptions keyVaultOptions) {
    final KeyPair keys = this.nacl.generateNewKeys();

    final StringBuilder publicId = new StringBuilder();
    final StringBuilder privateId = new StringBuilder();

    if (filename != null) {
      final Path path = Paths.get(filename);
      final String keyVaultId = path.getFileName().toString();

      if (!keyVaultId.matches("^[0-9a-zA-Z\\-]*$")) {
        throw new UnsupportedCharsetException(
            "Generated key ID for Azure Key Vault can contain only 0-9, a-z, A-Z and - characters");
      }

      publicId.append(keyVaultId);
      privateId.append(keyVaultId);
    }

    publicId.append("Pub");
    privateId.append("Key");

    SetSecretResponse pubResp = saveKeyInVault(publicId.toString(), keys.getPublicKey());
    SetSecretResponse privResp = saveKeyInVault(privateId.toString(), keys.getPrivateKey());

    AzureVaultKeyPair keyPair =
        new AzureVaultKeyPair(
            publicId.toString(),
            privateId.toString(),
            pubResp.getProperty("version"),
            privResp.getProperty("version"));

    return new GeneratedKeyPair(keyPair, keys.getPublicKey().encodeToBase64());
  }

  private SetSecretResponse saveKeyInVault(String id, Key key) {
    SetSecretResponse resp =
        keyVaultService.setSecret(Map.of("secretName", id, "secret", key.encodeToBase64()));
    LOGGER.debug("Key {} saved to vault with id {}", key.encodeToBase64(), id);
    return resp;
  }
}
