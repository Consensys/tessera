package com.quorum.tessera.util;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MyServiceClientCredentialsTest {

   private MyServiceClientCredentials instance;

   ExecutorService executorService;

   private AuthenticationContext authenticationContext;

   @Before
   public void onSetUp() {
       executorService = mock(ExecutorService.class);
       authenticationContext = mock(AuthenticationContext.class);
       instance = new MyServiceClientCredentials("clienid","clientSecret",executorService);
       instance.setAuthenticationContext(authenticationContext);
   }

   @After
   public void onTearDown() {
       verifyNoMoreInteractions(authenticationContext);

   }


   @Test
   public void doStuff() throws Exception {

       Future<AuthenticationResult> response = mock(Future.class);

       AuthenticationResult authenticationResult = new AuthenticationResult(null,"HELLPOW",null,0L,null,null,false);

       when(response.get()).thenReturn(authenticationResult);
        when(authenticationContext.acquireToken(anyString(), any(ClientCredential.class), any()))
            .thenReturn(response);

       String result = instance.doAuthenticate("auth","resource",null);

       assertThat(result).isEqualTo("HELLPOW");
       verify(authenticationContext).acquireToken(anyString(), any(ClientCredential.class), any());


   }

    @Test(expected = NullPointerException.class)
    public void doStuffNOAuth() throws Exception {
        instance.setAuthenticationContext(null);
        instance.doAuthenticate("https:///bogus","resource",null);
    }
}
