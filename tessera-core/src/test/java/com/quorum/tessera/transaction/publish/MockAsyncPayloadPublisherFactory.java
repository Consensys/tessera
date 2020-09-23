
package com.quorum.tessera.transaction.publish;

import static org.mockito.Mockito.mock;

public class MockAsyncPayloadPublisherFactory implements AsyncPayloadPublisherFactory {

    @Override
    public AsyncPayloadPublisher create(PayloadPublisher publisher) {
        return mock(AsyncPayloadPublisher.class);
    }
}
