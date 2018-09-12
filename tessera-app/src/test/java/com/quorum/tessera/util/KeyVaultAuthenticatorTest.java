package com.quorum.tessera.util;

import com.microsoft.rest.credentials.ServiceClientCredentials;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class KeyVaultAuthenticatorTest {

    private KeyVaultAuthenticator authenticator;

    private ServiceClientCredentials serviceClientCredentials;

    @Before
    public void onSetUp() {
        serviceClientCredentials = mock(ServiceClientCredentials.class);

        authenticator = new KeyVaultAuthenticator("bogus","bogus",serviceClientCredentials);
    }

    @After
    public void onTearDOwn() {

        verifyNoMoreInteractions(serviceClientCredentials);
    }

    @Test
    public void doStuff() {
        authenticator.getAuthenticatedClient();
        verify(serviceClientCredentials).applyCredentialsFilter(any());

    }
}
