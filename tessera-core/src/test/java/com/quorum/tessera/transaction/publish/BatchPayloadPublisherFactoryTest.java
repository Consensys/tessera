package com.quorum.tessera.transaction.publish;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class BatchPayloadPublisherFactoryTest {

  @Test
  public void newFactory() {
    BatchPayloadPublisherFactory factory = BatchPayloadPublisherFactory.newFactory();
    assertThat(factory).isNotNull();
  }
}
