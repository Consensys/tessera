package com.quorum.tessera.transaction.publish;

import java.util.ServiceLoader;

public interface BatchPayloadPublisherFactory {

    BatchPayloadPublisher create(PayloadPublisher publisher);

    static BatchPayloadPublisherFactory newFactory() {
        return ServiceLoader.load(BatchPayloadPublisherFactory.class)
            .findFirst().get();
    }

}
