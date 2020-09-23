package com.quorum.tessera.q2t;

import com.quorum.tessera.transaction.publish.AsyncPayloadPublisher;
import com.quorum.tessera.transaction.publish.AsyncPayloadPublisherFactory;
import com.quorum.tessera.transaction.publish.PayloadPublisher;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class RestAsyncPayloadPublisherFactoryTest {

    @Test
    public void create() {
        AsyncPayloadPublisherFactory factory = new RestAsyncPayloadPublisherFactory();
        AsyncPayloadPublisher publisher = factory.create(mock(PayloadPublisher.class));

        assertThat(publisher).isNotNull();
    }

}
