package com.quorum.tessera.recovery.workflow;

import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.recovery.resend.ResendBatchPublisher;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class BatchWorkflowFactoryProviderTest {

    @Test
    public void defaulConstructorForCoverage() {
        assertThat(new BatchWorkflowFactoryProvider()).isNotNull();
    }

    @Test
    public void provider() {

        try(
            var staticEnclave = mockStatic(Enclave.class);
            var staticDiscovery = mockStatic(Discovery.class);
            var staticResendBatchPublisher = mockStatic(ResendBatchPublisher.class);
            var staticPayloadEncoder = mockStatic(PayloadEncoder.class)
        ) {
            staticEnclave.when(Enclave::create).thenReturn(mock(Enclave.class));
            staticDiscovery.when(Discovery::create).thenReturn(mock(Discovery.class));
            staticResendBatchPublisher.when(ResendBatchPublisher::create).thenReturn(mock(ResendBatchPublisher.class));
            staticPayloadEncoder.when(PayloadEncoder::create).thenReturn(mock(PayloadEncoder.class));

            BatchWorkflowFactory batchWorkflowFactory = BatchWorkflowFactoryProvider.provider();
            assertThat(batchWorkflowFactory).isNotNull().isExactlyInstanceOf(BatchWorkflowFactoryImpl.class);

            staticEnclave.verify(Enclave::create);
            staticDiscovery.verify(Discovery::create);
            staticResendBatchPublisher.verify(ResendBatchPublisher::create);
            staticPayloadEncoder.verify(PayloadEncoder::create);

            staticEnclave.verifyNoMoreInteractions();
            staticDiscovery.verifyNoMoreInteractions();
            staticResendBatchPublisher.verifyNoMoreInteractions();
            staticPayloadEncoder.verifyNoMoreInteractions();
        }
    }

}
