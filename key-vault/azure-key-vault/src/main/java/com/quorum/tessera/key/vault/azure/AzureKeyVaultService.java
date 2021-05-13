package com.quorum.tessera.key.vault.azure;

import com.azure.core.exception.ResourceNotFoundException;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.quorum.tessera.config.vault.data.AzureGetSecretData;
import com.quorum.tessera.config.vault.data.AzureSetSecretData;
import com.quorum.tessera.key.vault.KeyVaultService;
import com.quorum.tessera.key.vault.VaultSecretNotFoundException;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AzureKeyVaultService
    implements KeyVaultService<AzureSetSecretData, AzureGetSecretData> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AzureKeyVaultService.class);

  private AzureSecretClientDelegate secretClient;

  AzureKeyVaultService(AzureSecretClientDelegate azureSecretClientDelegate) {
    this.secretClient = Objects.requireNonNull(azureSecretClientDelegate);
  }

  @Override
  public String getSecret(AzureGetSecretData azureGetSecretData) {
    final KeyVaultSecret secret;
    try {
      LOGGER.debug(
          "SecretName : {} , SecretVersion: {}",
          azureGetSecretData.getSecretName(),
          azureGetSecretData.getSecretVersion());
      secret =
          secretClient.getSecret(
              azureGetSecretData.getSecretName(), azureGetSecretData.getSecretVersion());
      LOGGER.debug("secret.id {}", secret.getId());
    } catch (ResourceNotFoundException e) {
      throw new VaultSecretNotFoundException(
          "Azure Key Vault secret "
              + azureGetSecretData.getSecretName()
              + " was not found in vault "
              + secretClient.getVaultUrl());
    }
    return secret.getValue();
  }

  @Override
  public Object setSecret(AzureSetSecretData azureSetSecretData) {
    return secretClient.setSecret(
        azureSetSecretData.getSecretName(), azureSetSecretData.getSecret());
  }
}
