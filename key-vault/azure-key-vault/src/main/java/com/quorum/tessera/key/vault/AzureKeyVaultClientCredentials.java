package com.quorum.tessera.key.vault;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.ClientCredential;
import com.microsoft.azure.keyvault.authentication.KeyVaultCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

public class AzureKeyVaultClientCredentials extends KeyVaultCredentials {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureKeyVaultClientCredentials.class);

    private final String clientId;

    private final String clientSecret;

    private AuthenticationContext authenticationContext;

    private final ExecutorService service;

    public AzureKeyVaultClientCredentials(String clientId, String clientSecret, ExecutorService service) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.service = Objects.requireNonNull(service);
    }

    protected void setAuthenticationContext(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    @Override
    public String doAuthenticate(String authorization, String resource, String scope) {
        try {
            if(Objects.isNull(authenticationContext)) {
                this.authenticationContext = new AuthenticationContext(authorization, false, service);
            }
            if(clientId == null || clientSecret == null) {
                throw new RuntimeException("AZURE_CLIENT_ID and AZURE_CLIENT_SECRET environment variables must be set");
            }
            ClientCredential credential = new ClientCredential(clientId,clientSecret);

            return authenticationContext.acquireToken(resource, credential, null).get().getAccessToken();

        } catch (ExecutionException  | MalformedURLException ex) {
            throw new RuntimeException(ex);
        } catch(InterruptedException ex) {
            LOGGER.warn("Key vault executor service interrupted");
            throw new RuntimeException(ex);
        }
    }
}
