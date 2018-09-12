package com.quorum.tessera.key.vault;

import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class KeyVaultClientDelegateTest {
    @Test(expected = RuntimeException.class)
    public void getSecretCallsClient() {

        ServiceClientCredentials serviceClientCredentials = mock(ServiceClientCredentials.class);
        KeyVaultClient keyVaultClient = new KeyVaultClient(serviceClientCredentials);

        KeyVaultClientDelegate keyVaultClientDelegate = new KeyVaultClientDelegate(keyVaultClient);

        keyVaultClientDelegate.getSecret("bogus","secret");

    }



}
