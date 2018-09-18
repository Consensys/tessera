package com.quorum.tessera.key.vault;

import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.rest.credentials.ServiceClientCredentials;

import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;


/**
 * Authenticates to Azure Key Vault by providing a callback to authenticate
 * using adal.
 */
public class KeyVaultClientFactory {

    private final ServiceClientCredentials serviceClientCredentials;

    private ExecutorService executorService;

    public KeyVaultClientFactory(ExecutorService executorService) {
        String clientId = System.getenv("AZURE_CLIENT_ID");
        String clientSecret = System.getenv("AZURE_CLIENT_SECRET");
        this.serviceClientCredentials = new KeyVaultClientCredentials(clientId, clientSecret, executorService);
        this.executorService = executorService;
    }

    @PreDestroy
    public void onDestroy() {
        executorService.shutdown();
    }

    public KeyVaultClient getAuthenticatedClient() {
        return new KeyVaultClient(serviceClientCredentials);
    }
}
