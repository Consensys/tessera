package com.quorum.tessera.key.vault;

import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.rest.credentials.ServiceClientCredentials;

public class AzureKeyVaultClientFactory {

    private final ServiceClientCredentials clientCredentials;

    public AzureKeyVaultClientFactory(ServiceClientCredentials clientCredentials) {
        this.clientCredentials = clientCredentials;
    }

    public KeyVaultClient getAuthenticatedClient() {
        return new KeyVaultClient(clientCredentials);
    }
}
