package com.quorum.tessera.sync;

import com.quorum.tessera.partyinfo.ResendRequest;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.encryption.PublicKey;
import java.util.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Set;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class TransactionRequesterTest {

    private static final PublicKey KEY_ONE = PublicKey.from(new byte[] {1});

    private static final PublicKey KEY_TWO = PublicKey.from(new byte[] {2});

    private Enclave enclave;

    private ResendClient resendClient;

    private TransactionRequester transactionRequester;

    @Before
    public void init() {

        this.enclave = mock(Enclave.class);
        this.resendClient = mock(ResendClient.class);

        doReturn(true).when(resendClient).makeResendRequest(anyString(), any(ResendRequest.class));

        this.transactionRequester = new TransactionRequesterImpl(enclave, resendClient);
    }

    @After
    public void after() {
        verifyNoMoreInteractions(enclave, resendClient);
    }

    @Test
    public void noPublicKeysMakesNoCalls() {
        when(enclave.getPublicKeys()).thenReturn(Collections.emptySet());

        this.transactionRequester.requestAllTransactionsFromNode("fakeurl.com");

        verifyZeroInteractions(resendClient);
        verify(enclave).getPublicKeys();
    }

    @Test
    public void multipleKeysMakesCorrectCalls() {

        final Set<PublicKey> allKeys = Stream.of(KEY_ONE, KEY_TWO).collect(Collectors.toSet());

        when(enclave.getPublicKeys()).thenReturn(allKeys);

        this.transactionRequester.requestAllTransactionsFromNode("fakeurl1.com");

        final ArgumentCaptor<ResendRequest> captor = ArgumentCaptor.forClass(ResendRequest.class);
        verify(resendClient, times(2)).makeResendRequest(eq("fakeurl1.com"), captor.capture());
        verify(enclave).getPublicKeys();

        String encodedKeyOne = Base64.getEncoder().encodeToString(KEY_ONE.getKeyBytes());
        String encodedKeyTwo = Base64.getEncoder().encodeToString(KEY_TWO.getKeyBytes());

        assertThat(captor.getAllValues())
                .hasSize(2)
                .extracting("publicKey")
                .containsExactlyInAnyOrder(encodedKeyOne, encodedKeyTwo);
    }

    @Test
    public void failedCallRetries() {
        when(enclave.getPublicKeys()).thenReturn(Collections.singleton(KEY_ONE));

        when(resendClient.makeResendRequest(anyString(), any(ResendRequest.class))).thenReturn(false);

        this.transactionRequester.requestAllTransactionsFromNode("fakeurl.com");

        verify(resendClient, times(5)).makeResendRequest(eq("fakeurl.com"), any(ResendRequest.class));
        verify(enclave).getPublicKeys();
    }

    @Test
    public void calltoPostDelegateThrowsException() {

        when(enclave.getPublicKeys()).thenReturn(Collections.singleton(KEY_ONE));
        when(resendClient.makeResendRequest(anyString(), any(ResendRequest.class))).thenThrow(RuntimeException.class);

        this.transactionRequester.requestAllTransactionsFromNode("fakeurl.com");

        verify(resendClient, times(5)).makeResendRequest(eq("fakeurl.com"), any(ResendRequest.class));
        verify(enclave).getPublicKeys();
    }
}
