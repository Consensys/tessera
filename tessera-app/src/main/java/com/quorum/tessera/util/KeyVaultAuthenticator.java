package com.quorum.tessera.util;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.authentication.KeyVaultCredentials;
import com.microsoft.rest.credentials.ServiceClientCredentials;

import javax.annotation.PreDestroy;
import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * Authenticates to Azure Key Vault by providing a callback to authenticate
 * using adal.
 */
public class KeyVaultAuthenticator {

    private final String clientId;

    private final String clientSecret;

    private final ServiceClientCredentials serviceClientCredentials;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public KeyVaultAuthenticator(String clientId, String clientSecret, ServiceClientCredentials serviceClientCredentials) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.serviceClientCredentials = serviceClientCredentials;
    }

    @PreDestroy
    public void onDestroy() {
        executorService.shutdown();
    }

    public KeyVaultClient getAuthenticatedClient() {
        return new KeyVaultClient(serviceClientCredentials);
    }
}
