package com.quorum.tessera.p2p.recovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.recovery.resend.BatchTransactionRequester;
import org.junit.Test;

public class BatchTransactionRequesterProviderTest {

  @Test
  public void provider() {

    try (var enclaveMockedStatic = mockStatic(Enclave.class);
        var recoveryClientMockedStatic = mockStatic(RecoveryClient.class)) {
      enclaveMockedStatic.when(Enclave::create).thenReturn(mock(Enclave.class));
      recoveryClientMockedStatic
          .when(RecoveryClient::create)
          .thenReturn(mock(RecoveryClient.class));

      BatchTransactionRequester batchTransactionRequester =
          BatchTransactionRequesterProvider.provider();
      assertThat(batchTransactionRequester)
          .isNotNull()
          .isExactlyInstanceOf(RestBatchTransactionRequester.class);

      enclaveMockedStatic.verify(Enclave::create);
      recoveryClientMockedStatic.verify(RecoveryClient::create);
    }
  }

  @Test
  public void defaultConstructorForCoverage() {
    assertThat(new BatchTransactionRequesterProvider()).isNotNull();
  }
}
