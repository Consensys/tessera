package com.quorum.tessera.recovery.workflow.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.recovery.resend.ResendBatchPublisher;
import com.quorum.tessera.recovery.workflow.BatchWorkflowFactory;
import org.junit.Test;

public class BatchWorkflowFactoryProviderTest {

  @Test
  public void defaultConstructorForCoverage() {
    assertThat(new BatchWorkflowFactoryProvider()).isNotNull();
  }

  @Test
  public void provider() {

    try (var staticEnclave = mockStatic(Enclave.class);
        var staticDiscovery = mockStatic(Discovery.class);
        var staticResendBatchPublisher = mockStatic(ResendBatchPublisher.class)) {
      staticEnclave.when(Enclave::create).thenReturn(mock(Enclave.class));
      staticDiscovery.when(Discovery::create).thenReturn(mock(Discovery.class));
      staticResendBatchPublisher
          .when(ResendBatchPublisher::create)
          .thenReturn(mock(ResendBatchPublisher.class));

      BatchWorkflowFactory batchWorkflowFactory = BatchWorkflowFactoryProvider.provider();
      assertThat(batchWorkflowFactory)
          .isNotNull()
          .isExactlyInstanceOf(BatchWorkflowFactoryImpl.class);

      staticEnclave.verify(Enclave::create);
      staticDiscovery.verify(Discovery::create);
      staticResendBatchPublisher.verify(ResendBatchPublisher::create);

      staticEnclave.verifyNoMoreInteractions();
      staticDiscovery.verifyNoMoreInteractions();
      staticResendBatchPublisher.verifyNoMoreInteractions();
    }
  }
}
