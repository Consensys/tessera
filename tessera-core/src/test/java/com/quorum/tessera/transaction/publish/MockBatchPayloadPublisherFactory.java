package com.quorum.tessera.transaction.publish;

import static org.mockito.Mockito.mock;

public class MockBatchPayloadPublisherFactory implements BatchPayloadPublisherFactory {

  @Override
  public BatchPayloadPublisher create(PayloadPublisher publisher) {
    return mock(BatchPayloadPublisher.class);
  }
}
