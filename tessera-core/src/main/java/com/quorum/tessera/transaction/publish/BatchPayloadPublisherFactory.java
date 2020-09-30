package com.quorum.tessera.transaction.publish;

import com.quorum.tessera.ServiceLoaderUtil;

public interface BatchPayloadPublisherFactory {

    BatchPayloadPublisher create(PayloadPublisher publisher);

    static BatchPayloadPublisherFactory newFactory() {
        return ServiceLoaderUtil.load(BatchPayloadPublisherFactory.class).get();
    }

}
