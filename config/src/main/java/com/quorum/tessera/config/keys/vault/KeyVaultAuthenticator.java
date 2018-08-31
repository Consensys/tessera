package com.quorum.tessera.config.keys.vault;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.authentication.KeyVaultCredentials;
import com.microsoft.rest.credentials.ServiceClientCredentials;

import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * Authenticates to Azure Key Vault by providing a callback to authenticate
 * using adal.
 *
 */
public class KeyVaultAuthenticator {

    public static KeyVaultClient getAuthenticatedClient() {
        return new KeyVaultClient(createCredentials());
    }

    /**
     * Creates a new KeyVaultCredential based on the access token obtained.
     * @return
     */
    private static ServiceClientCredentials createCredentials() {
        return new KeyVaultCredentials() {
            //Callback that supplies the token type and access token on request.
            @Override
            public String doAuthenticate(String authorization, String resource, String scope) {
                AuthenticationResult authResult;

                try {
                    authResult = getAuthenticationResult(authorization, resource);

                    return authResult.getAccessToken();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }
        };
    }

    /**
     * Private helper method that gets the access token for the authorization and resource depending on which variables are supplied in the environment.
     *
     * @param authorization
     * @param resource
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws MalformedURLException
     * @throws Exception
     */
    private static AuthenticationResult getAuthenticationResult(String authorization, String resource) throws InterruptedException, MalformedURLException, ExecutionException {
        String clientId = System.getenv("AZURE_CLIENT_ID");
        String clientSecret = System.getenv("AZURE_CLIENT_SECRET");

        AuthenticationResult result = null;

        //Starts a service to fetch access token
        ExecutorService service = null;
        try {
            service = Executors.newFixedThreadPool(1);
            AuthenticationContext context = new AuthenticationContext(authorization, false, service);

            Future<AuthenticationResult> future = null;

            //Acquires token based on client ID and client secret
            if(clientId != null && clientSecret != null) {
                ClientCredential credential = new ClientCredential(clientId, clientSecret);
                future = context.acquireToken(resource, credential, null);
            }

            result = future.get();
        } finally {
            service.shutdown();
        }

        if(result == null) {
            throw new RuntimeException("Azure authentication not successful");
        }

        return result;
    }
}
