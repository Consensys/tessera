package com.quorum.tessera.p2p.recovery;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.recovery.resend.BatchTransactionRequester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Base64;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class RestBatchTransactionRequesterTest {

    private static final PublicKey KEY_ONE = PublicKey.from(new byte[] {1});

    private static final PublicKey KEY_TWO = PublicKey.from(new byte[] {2});

    private Enclave enclave;

    private RecoveryClient p2pClient;

    private BatchTransactionRequester transactionRequester;

    @Before
    public void init() {

        this.enclave = mock(Enclave.class);
        this.p2pClient = mock(RecoveryClient.class);

        doReturn(new ResendBatchResponse(100))
                .when(p2pClient)
                .makeBatchResendRequest(anyString(), any(ResendBatchRequest.class));

        this.transactionRequester = new RestBatchTransactionRequester(enclave, p2pClient, 100);
    }

    @After
    public void after() {
        verifyNoMoreInteractions(enclave, p2pClient);
    }

    @Test
    public void noPublicKeysMakesNoCalls() {
        when(enclave.getPublicKeys()).thenReturn(Collections.emptySet());

        this.transactionRequester.requestAllTransactionsFromNode("fakeurl.com");

        verifyZeroInteractions(p2pClient);
        verify(enclave).getPublicKeys();
    }

    @Test
    public void multipleKeysMakesCorrectCalls() {

        final Set<PublicKey> allKeys = Stream.of(KEY_ONE, KEY_TWO).collect(Collectors.toSet());

        when(enclave.getPublicKeys()).thenReturn(allKeys);

        this.transactionRequester.requestAllTransactionsFromNode("fakeurl1.com");

        final ArgumentCaptor<ResendBatchRequest> captor = ArgumentCaptor.forClass(ResendBatchRequest.class);
        verify(p2pClient, times(2)).makeBatchResendRequest(eq("fakeurl1.com"), captor.capture());
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

        when(p2pClient.makeBatchResendRequest(anyString(), any(ResendBatchRequest.class))).thenReturn(null);

        this.transactionRequester.requestAllTransactionsFromNode("fakeurl.com");

        verify(p2pClient, times(5)).makeBatchResendRequest(eq("fakeurl.com"), any(ResendBatchRequest.class));
        verify(enclave).getPublicKeys();
    }

    @Test
    public void calltoPostDelegateThrowsException() {

        when(enclave.getPublicKeys()).thenReturn(Collections.singleton(KEY_ONE));
        when(p2pClient.makeBatchResendRequest(anyString(), any(ResendBatchRequest.class)))
                .thenThrow(RuntimeException.class);

        this.transactionRequester.requestAllTransactionsFromNode("fakeurl.com");

        verify(p2pClient, times(5)).makeBatchResendRequest(eq("fakeurl.com"), any(ResendBatchRequest.class));
        verify(enclave).getPublicKeys();
    }

    @Test
    public void requestFromLegacyNode() {
        assertThat(transactionRequester.requestAllTransactionsFromLegacyNode("test")).isFalse();
    }
}
