package com.quorum.tessera.key.vault;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class KeyVaultClientCredentialsTest {
    private KeyVaultClientCredentials credentials;

    private ExecutorService executorService;

    private AuthenticationContext authenticationContext;

    @Before
    public void onSetUp() {
        executorService = mock(ExecutorService.class);
        authenticationContext = mock(AuthenticationContext.class);
        credentials = new KeyVaultClientCredentials("clientId","clientSecret", executorService);
        credentials.setAuthenticationContext(authenticationContext);
    }

    @Test
    public void getCredentialsUsingInjectedParameters() throws Exception {
        Future<AuthenticationResult> response = mock(Future.class);

        AuthenticationResult authenticationResult = new AuthenticationResult(null,"accessToken",null,0L,null,null,false);

        when(response.get()).thenReturn(authenticationResult);
        when(authenticationContext.acquireToken(anyString(), any(ClientCredential.class), any())).thenReturn(response);

        String result = credentials.doAuthenticate("auth","resource",null);

        assertThat(result).isEqualTo("accessToken");
        verify(authenticationContext).acquireToken(anyString(), any(ClientCredential.class), any());
        verifyNoMoreInteractions(authenticationContext);
    }

    @Test
    public void urlMustIncludeProtocol() {
        credentials.setAuthenticationContext(null);
        final Throwable ex = catchThrowable(() -> credentials.doAuthenticate("url", null, null));

        assertThat(ex).isInstanceOf(RuntimeException.class)
        .hasMessageContaining("no protocol");
    }

    @Test
    public void urlMustIncludeOneSegmentInPath() {
        credentials.setAuthenticationContext(null);
        final Throwable ex = catchThrowable(() -> credentials.doAuthenticate("https://url", null, null));

        assertThat(ex).isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Uri should have at least one segment in the path");
    }

    @Test
    public void urlMustUseHttps() {
        credentials.setAuthenticationContext(null);
        final Throwable ex = catchThrowable(() -> credentials.doAuthenticate("http://url/path", null, null));

        assertThat(ex).isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("should use the 'https' scheme");
    }

    @Test
    public void resourceMayNotBeNull() {
        credentials.setAuthenticationContext(null);
        String goodUrl = "https://url/path";
        final Throwable ex = catchThrowable(() -> credentials.doAuthenticate(goodUrl, null, null));

        assertThat(ex).isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("resource is null or empty");
    }

    //this null pointer exception is thrown when authenticationContext.acquireToken called, not due to null scope
    @Test(expected = NullPointerException.class)
    public void scopeCanBeNull() {
        credentials.setAuthenticationContext(null);
        String goodUrl = "https://host/path";
        credentials.doAuthenticate(goodUrl,"resource",null);
    }

    @Test(expected = RuntimeException.class)
    public void ifFutureAbortsThenExecutionExceptionThrownAsRuntimeException() throws ExecutionException, InterruptedException {
        Future<AuthenticationResult> response = mock(Future.class);
        when(response.get()).thenThrow(ExecutionException.class);
        when(authenticationContext.acquireToken(anyString(), any(ClientCredential.class), any())).thenReturn(response);

        credentials.doAuthenticate("auth", "resource", null);
    }

    @Test(expected = RuntimeException.class)
    public void threadInterruptedExceptionIsCaughtAndLogged() throws ExecutionException, InterruptedException {
        Future<AuthenticationResult> response = mock(Future.class);
        when(response.get()).thenThrow(InterruptedException.class);
        when(authenticationContext.acquireToken(anyString(), any(ClientCredential.class), any())).thenReturn(response);

        credentials.doAuthenticate("auth", "resource", null);
    }

    @Test
    public void nullClientIdThrowsRuntimeException() {
        credentials = new KeyVaultClientCredentials(null, "secret", executorService);

        String goodUrl = "https://url/path";
        final Throwable ex = catchThrowable(() -> credentials.doAuthenticate(goodUrl, null, null));

        assertThat(ex).isInstanceOf(RuntimeException.class)
            .hasMessageContaining("AZURE_CLIENT_ID and AZURE_CLIENT_SECRET environment variables must be set");
    }

    @Test
    public void nullClientSecretThrowsRuntimeException() {
        credentials = new KeyVaultClientCredentials("id", null, executorService);

        String goodUrl = "https://url/path";
        final Throwable ex = catchThrowable(() -> credentials.doAuthenticate(goodUrl, null, null));

        assertThat(ex).isInstanceOf(RuntimeException.class)
            .hasMessageContaining("AZURE_CLIENT_ID and AZURE_CLIENT_SECRET environment variables must be set");
    }

    @Test
    public void nullClientIdAndSecretThrowsRuntimeException() {
        credentials = new KeyVaultClientCredentials(null, null, executorService);

        String goodUrl = "https://url/path";
        final Throwable ex = catchThrowable(() -> credentials.doAuthenticate(goodUrl, null, null));

        assertThat(ex).isInstanceOf(RuntimeException.class)
            .hasMessageContaining("AZURE_CLIENT_ID and AZURE_CLIENT_SECRET environment variables must be set");
    }
}
