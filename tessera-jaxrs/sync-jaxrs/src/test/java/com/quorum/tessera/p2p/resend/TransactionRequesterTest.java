package com.quorum.tessera.p2p.resend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.encryption.PublicKey;
import java.util.Collections;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

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

    final boolean success = this.transactionRequester.requestAllTransactionsFromNode("fakeurl.com");

    assertThat(success).isTrue();

    verifyNoInteractions(resendClient);
    verify(enclave).getPublicKeys();
  }

  @Test
  public void multipleKeysMakesCorrectCalls() {
    final Set<PublicKey> allKeys = Stream.of(KEY_ONE, KEY_TWO).collect(Collectors.toSet());

    when(enclave.getPublicKeys()).thenReturn(allKeys);

    final boolean success =
        this.transactionRequester.requestAllTransactionsFromNode("fakeurl1.com");

    assertThat(success).isTrue();

    final ArgumentCaptor<ResendRequest> captor = ArgumentCaptor.forClass(ResendRequest.class);
    verify(resendClient, times(2)).makeResendRequest(eq("fakeurl1.com"), captor.capture());
    verify(enclave).getPublicKeys();

    assertThat(captor.getAllValues())
        .hasSize(2)
        .extracting("publicKey")
        .containsExactlyInAnyOrder(KEY_ONE.encodeToBase64(), KEY_TWO.encodeToBase64());
  }

  @Test
  public void callToPostDelegateThrowsException() {
    when(enclave.getPublicKeys()).thenReturn(Collections.singleton(KEY_ONE));
    when(resendClient.makeResendRequest(anyString(), any(ResendRequest.class)))
        .thenThrow(RuntimeException.class);

    final boolean success = this.transactionRequester.requestAllTransactionsFromNode("fakeurl.com");

    assertThat(success).isFalse();

    verify(resendClient).makeResendRequest(eq("fakeurl.com"), any(ResendRequest.class));
    verify(enclave).getPublicKeys();
  }

  @Test
  public void create() {

    TransactionRequester expected = mock(TransactionRequester.class);

    TransactionRequester result;
    try (var serviceLoaderMockedStatic = mockStatic(ServiceLoader.class)) {
      ServiceLoader<TransactionRequester> serviceLoader = mock(ServiceLoader.class);
      when(serviceLoader.findFirst()).thenReturn(Optional.of(expected));
      serviceLoaderMockedStatic
          .when(() -> ServiceLoader.load(TransactionRequester.class))
          .thenReturn(serviceLoader);

      result = TransactionRequester.create();

      verify(serviceLoader).findFirst();
      verifyNoMoreInteractions(serviceLoader);

      serviceLoaderMockedStatic.verify(() -> ServiceLoader.load(TransactionRequester.class));
      serviceLoaderMockedStatic.verifyNoMoreInteractions();
    }
    assertThat(result).isSameAs(expected);
  }
}
