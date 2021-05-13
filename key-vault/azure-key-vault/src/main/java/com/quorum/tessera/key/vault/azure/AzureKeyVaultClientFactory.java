package com.quorum.tessera.key.vault.azure;

import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.rest.credentials.ServiceClientCredentials;

class AzureKeyVaultClientFactory {

  private final ServiceClientCredentials clientCredentials;

  AzureKeyVaultClientFactory(ServiceClientCredentials clientCredentials) {
    this.clientCredentials = clientCredentials;
  }

  KeyVaultClient getAuthenticatedClient() {
    return new KeyVaultClient(clientCredentials);
  }
}
