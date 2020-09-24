package com.quorum.tessera.transaction.publish;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BatchPayloadPublisherFactoryTest {

    @Test
    public void newFactory() {
        BatchPayloadPublisherFactory factory = BatchPayloadPublisherFactory.newFactory();
        assertThat(factory).isNotNull();
    }

}
