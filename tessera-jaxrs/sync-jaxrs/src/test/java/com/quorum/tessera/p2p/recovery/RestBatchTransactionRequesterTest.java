package com.quorum.tessera.p2p.recovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.p2p.resend.ResendRequest;
import com.quorum.tessera.recovery.resend.BatchTransactionRequester;
import java.util.Base64;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class RestBatchTransactionRequesterTest {

  private static final PublicKey KEY_ONE = PublicKey.from(new byte[] {1});

  private static final PublicKey KEY_TWO = PublicKey.from(new byte[] {2});

  private Enclave enclave;

  private RecoveryClient recoveryClient;

  private BatchTransactionRequester transactionRequester;

  @Before
  public void beforeTest() {

    this.enclave = mock(Enclave.class);
    this.recoveryClient = mock(RecoveryClient.class);

    doReturn(new ResendBatchResponse(100L))
        .when(recoveryClient)
        .makeBatchResendRequest(anyString(), any(ResendBatchRequest.class));

    doReturn(true).when(recoveryClient).makeResendRequest(anyString(), any(ResendRequest.class));

    this.transactionRequester = new RestBatchTransactionRequester(enclave, recoveryClient, 100);
  }

  @After
  public void afterTest() {
    verifyNoMoreInteractions(enclave, recoveryClient);
  }

  @Test
  public void noPublicKeysMakesNoCalls() {
    when(enclave.getPublicKeys()).thenReturn(Collections.emptySet());

    this.transactionRequester.requestAllTransactionsFromNode("fakeurl.com");

    verifyNoInteractions(recoveryClient);
    verify(enclave).getPublicKeys();
  }

  @Test
  public void multipleKeysMakesCorrectCalls() {

    final Set<PublicKey> allKeys = Stream.of(KEY_ONE, KEY_TWO).collect(Collectors.toSet());

    when(enclave.getPublicKeys()).thenReturn(allKeys);

    this.transactionRequester.requestAllTransactionsFromNode("fakeurl1.com");

    final ArgumentCaptor<ResendBatchRequest> captor =
        ArgumentCaptor.forClass(ResendBatchRequest.class);
    verify(recoveryClient, times(2)).makeBatchResendRequest(eq("fakeurl1.com"), captor.capture());
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

    when(recoveryClient.makeBatchResendRequest(anyString(), any(ResendBatchRequest.class)))
        .thenReturn(null);

    this.transactionRequester.requestAllTransactionsFromNode("fakeurl.com");

    verify(recoveryClient, times(5))
        .makeBatchResendRequest(eq("fakeurl.com"), any(ResendBatchRequest.class));
    verify(enclave).getPublicKeys();
  }

  @Test
  public void calltoPostDelegateThrowsException() {

    when(enclave.getPublicKeys()).thenReturn(Collections.singleton(KEY_ONE));
    when(recoveryClient.makeBatchResendRequest(anyString(), any(ResendBatchRequest.class)))
        .thenThrow(RuntimeException.class);

    this.transactionRequester.requestAllTransactionsFromNode("fakeurl.com");

    verify(recoveryClient, times(5))
        .makeBatchResendRequest(eq("fakeurl.com"), any(ResendBatchRequest.class));
    verify(enclave).getPublicKeys();
  }

  @Test
  public void legacyRequestNoPublicKeysMakesNoCalls() {
    when(enclave.getPublicKeys()).thenReturn(Collections.emptySet());

    final boolean success =
        this.transactionRequester.requestAllTransactionsFromLegacyNode("fakeurl.com");

    assertThat(success).isTrue();

    verifyNoInteractions(recoveryClient);
    verify(enclave).getPublicKeys();
  }

  @Test
  public void legacyRequestMultipleKeysMakesCorrectCalls() {
    final Set<PublicKey> allKeys = Stream.of(KEY_ONE, KEY_TWO).collect(Collectors.toSet());

    when(enclave.getPublicKeys()).thenReturn(allKeys);

    final boolean success =
        this.transactionRequester.requestAllTransactionsFromLegacyNode("fakeurl1.com");

    assertThat(success).isTrue();

    final ArgumentCaptor<ResendRequest> captor = ArgumentCaptor.forClass(ResendRequest.class);
    verify(recoveryClient, times(2)).makeResendRequest(eq("fakeurl1.com"), captor.capture());
    verify(enclave).getPublicKeys();

    Assertions.assertThat(captor.getAllValues())
        .hasSize(2)
        .extracting("publicKey")
        .containsExactlyInAnyOrder(KEY_ONE.encodeToBase64(), KEY_TWO.encodeToBase64());
  }

  @Test
  public void legacyRequestCallToPostDelegateThrowsException() {
    when(enclave.getPublicKeys()).thenReturn(Collections.singleton(KEY_ONE));
    when(recoveryClient.makeResendRequest(anyString(), any(ResendRequest.class)))
        .thenThrow(RuntimeException.class);

    final boolean success =
        this.transactionRequester.requestAllTransactionsFromLegacyNode("fakeurl.com");

    assertThat(success).isFalse();

    verify(recoveryClient).makeResendRequest(eq("fakeurl.com"), any(ResendRequest.class));
    verify(enclave).getPublicKeys();
  }
}
