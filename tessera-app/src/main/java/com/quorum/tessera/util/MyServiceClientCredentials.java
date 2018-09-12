package com.quorum.tessera.util;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.ClientCredential;
import com.microsoft.azure.keyvault.authentication.KeyVaultCredentials;

import java.net.MalformedURLException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

public class MyServiceClientCredentials extends KeyVaultCredentials {

    private final String clientId;

    private final String clientSecret;

    private AuthenticationContext authenticationContext;

    private final ExecutorService service;

    public MyServiceClientCredentials(String clientId, String clientSecret, ExecutorService service) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.service = service;
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
            ClientCredential credential = new ClientCredential(clientId,clientSecret);

            return authenticationContext.acquireToken(resource, credential, null).get().getAccessToken();
        } catch (ExecutionException  | MalformedURLException ex) {
            throw new RuntimeException(ex);
        } catch(InterruptedException ex) {
            //TODO: Log warning
            throw new RuntimeException(ex);
        }
    }
}
