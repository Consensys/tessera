package com.quorum.tessera.q2t;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.quorum.tessera.enclave.EncodedPayloadCodec;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.q2t.internal.BatchPayloadPublisherProvider;
import com.quorum.tessera.transaction.publish.BatchPayloadPublisher;
import com.quorum.tessera.transaction.publish.PayloadPublisher;
import org.junit.Test;

public class BatchPayloadPublisherProviderTest {

  @Test
  public void provider() {

    try (var payloadEncoderMockedStatic = mockStatic(PayloadEncoder.class);
        var payloadPublisherMockedStatic = mockStatic(PayloadPublisher.class)) {
      payloadEncoderMockedStatic
          .when(() -> PayloadEncoder.create(any(EncodedPayloadCodec.class)))
          .thenReturn(mock(PayloadEncoder.class));
      payloadPublisherMockedStatic
          .when(PayloadPublisher::create)
          .thenReturn(mock(PayloadPublisher.class));

      BatchPayloadPublisher result = BatchPayloadPublisherProvider.provider();
      assertThat(result).isNotNull();
      payloadEncoderMockedStatic.verify(
          () -> PayloadEncoder.create(any(EncodedPayloadCodec.class)));
      payloadPublisherMockedStatic.verify(PayloadPublisher::create);
    }
  }

  @Test
  public void defaultConstructorForCoverage() {
    assertThat(new BatchPayloadPublisherProvider()).isNotNull();
  }
}
