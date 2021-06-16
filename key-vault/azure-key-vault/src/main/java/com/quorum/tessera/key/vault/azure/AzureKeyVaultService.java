package com.quorum.tessera.key.vault.azure;

import com.azure.core.exception.ResourceNotFoundException;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.quorum.tessera.key.vault.KeyVaultService;
import com.quorum.tessera.key.vault.VaultSecretNotFoundException;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AzureKeyVaultService implements KeyVaultService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AzureKeyVaultService.class);

  protected static final String SECRET_NAME_KEY = "secretName";

  protected static final String SECRET_KEY = "secret";

  protected static final String SECRET_VERSION_KEY = "secretVersion";

  private final SecretClient secretClient;

  AzureKeyVaultService(SecretClient secretClient) {
    this.secretClient = Objects.requireNonNull(secretClient);
  }

  @Override
  public String getSecret(Map<String, String> azureGetSecretData) {

    final String secretName = azureGetSecretData.get(SECRET_NAME_KEY);
    final String secretVersion = azureGetSecretData.get(SECRET_VERSION_KEY);

    final KeyVaultSecret secret;
    try {
      LOGGER.debug("SecretName : {} , SecretVersion: {}", secretName, secretVersion);
      secret = secretClient.getSecret(secretName, secretVersion);
      LOGGER.debug("secret.id {}", secret.getId());
    } catch (ResourceNotFoundException e) {
      throw new VaultSecretNotFoundException(
          "Azure Key Vault secret "
              + secretName
              + " was not found in vault "
              + secretClient.getVaultUrl());
    }
    return secret.getValue();
  }

  @Override
  public Object setSecret(Map<String, String> azureSetSecretData) {

    final String secretName = azureSetSecretData.get(SECRET_NAME_KEY);
    final String secret = azureSetSecretData.get(SECRET_KEY);

    return secretClient.setSecret(secretName, secret);
  }
}
