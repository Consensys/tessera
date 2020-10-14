package com.quorum.tessera.transaction.publish;

import com.quorum.tessera.loader.ServiceLoaderUtil;

public interface BatchPayloadPublisherFactory {

    BatchPayloadPublisher create(PayloadPublisher publisher);

    static BatchPayloadPublisherFactory newFactory() {
        return ServiceLoaderUtil.load(BatchPayloadPublisherFactory.class).get();
    }

}
