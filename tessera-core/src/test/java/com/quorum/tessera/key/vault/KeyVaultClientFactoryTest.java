package com.quorum.tessera.key.vault;

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class KeyVaultClientFactoryTest {

    private KeyVaultClientFactory keyVaultClientFactory;

    @Test
    public void injectedCredentialsUsedToGetClient() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        keyVaultClientFactory = new KeyVaultClientFactory(executorService);
        keyVaultClientFactory.getAuthenticatedClient();
    }

    @Test
    public void onDestroyShutsDownExecutor() {
        ExecutorService executorService = mock(ExecutorService.class);

        keyVaultClientFactory = new KeyVaultClientFactory(executorService);
        keyVaultClientFactory.onDestroy();

        verify(executorService).shutdown();
    }
}
