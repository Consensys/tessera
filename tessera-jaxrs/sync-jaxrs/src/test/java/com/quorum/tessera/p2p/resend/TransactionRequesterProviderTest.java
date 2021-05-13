package com.quorum.tessera.p2p.resend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.quorum.tessera.enclave.Enclave;
import org.junit.Test;

public class TransactionRequesterProviderTest {

  @Test
  public void provider() {

    try (var enclaveMockedStatic = mockStatic(Enclave.class);
        var resendClientMockedStatic = mockStatic(ResendClient.class)) {
      enclaveMockedStatic.when(Enclave::create).thenReturn(mock(Enclave.class));
      resendClientMockedStatic.when(ResendClient::create).thenReturn(mock(ResendClient.class));

      TransactionRequester transactionRequester = TransactionRequesterProvider.provider();
      assertThat(transactionRequester)
          .isNotNull()
          .isExactlyInstanceOf(TransactionRequesterImpl.class);

      enclaveMockedStatic.verify(Enclave::create);
      resendClientMockedStatic.verify(ResendClient::create);
    }
  }

  @Test
  public void defaultConstrcutorForCoverage() {
    assertThat(new TransactionRequesterProvider()).isNotNull();
  }
}
