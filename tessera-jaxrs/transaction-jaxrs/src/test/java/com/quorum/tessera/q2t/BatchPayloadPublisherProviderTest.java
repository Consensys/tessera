package com.quorum.tessera.q2t;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.quorum.tessera.q2t.internal.BatchPayloadPublisherProvider;
import com.quorum.tessera.transaction.publish.BatchPayloadPublisher;
import com.quorum.tessera.transaction.publish.PayloadPublisher;
import org.junit.Test;

public class BatchPayloadPublisherProviderTest {

  @Test
  public void provider() {

    try (var payloadPublisherMockedStatic = mockStatic(PayloadPublisher.class)) {
      payloadPublisherMockedStatic
          .when(PayloadPublisher::create)
          .thenReturn(mock(PayloadPublisher.class));

      BatchPayloadPublisher result = BatchPayloadPublisherProvider.provider();
      assertThat(result).isNotNull();
      payloadPublisherMockedStatic.verify(PayloadPublisher::create);
    }
  }
}
