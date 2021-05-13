package com.quorum.tessera.key.vault.azure;

import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.models.SecretBundle;
import com.microsoft.azure.keyvault.requests.SetSecretRequest;
import java.util.Objects;

class AzureKeyVaultClientDelegate {
  private final KeyVaultClient keyVaultClient;

  AzureKeyVaultClientDelegate(KeyVaultClient keyVaultClient) {
    this.keyVaultClient = Objects.requireNonNull(keyVaultClient);
  }

  SecretBundle getSecret(String vaultBaseUrl, String secretName) {
    return keyVaultClient.getSecret(vaultBaseUrl, secretName);
  }

  SecretBundle getSecret(String vaultBaseUrl, String secretName, String secretVersion) {
    return keyVaultClient.getSecret(vaultBaseUrl, secretName, secretVersion);
  }

  SecretBundle setSecret(SetSecretRequest setSecretRequest) {
    return keyVaultClient.setSecret(setSecretRequest);
  }
}
