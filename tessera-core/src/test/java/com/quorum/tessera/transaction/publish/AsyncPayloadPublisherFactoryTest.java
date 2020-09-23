package com.quorum.tessera.transaction.publish;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AsyncPayloadPublisherFactoryTest {

    @Test
    public void newFactory() {
        AsyncPayloadPublisherFactory factory = AsyncPayloadPublisherFactory.newFactory();
        assertThat(factory).isNotNull();
    }

}
