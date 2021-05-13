package com.quorum.tessera.q2t;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.quorum.tessera.transaction.publish.BatchPayloadPublisher;
import com.quorum.tessera.transaction.publish.BatchPayloadPublisherFactory;
import com.quorum.tessera.transaction.publish.PayloadPublisher;
import org.junit.Test;

public class AsyncBatchPayloadPublisherFactoryTest {

  @Test
  public void create() {
    BatchPayloadPublisherFactory factory = new AsyncBatchPayloadPublisherFactory();
    BatchPayloadPublisher publisher = factory.create(mock(PayloadPublisher.class));

    assertThat(publisher).isNotNull();
  }
}
