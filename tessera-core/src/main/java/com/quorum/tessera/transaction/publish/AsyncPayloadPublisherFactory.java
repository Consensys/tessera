package com.quorum.tessera.transaction.publish;

import com.quorum.tessera.ServiceLoaderUtil;

public interface AsyncPayloadPublisherFactory {

    AsyncPayloadPublisher create(PayloadPublisher publisher);

    static AsyncPayloadPublisherFactory newFactory() {
        return ServiceLoaderUtil.load(AsyncPayloadPublisherFactory.class).get();
    }

}
