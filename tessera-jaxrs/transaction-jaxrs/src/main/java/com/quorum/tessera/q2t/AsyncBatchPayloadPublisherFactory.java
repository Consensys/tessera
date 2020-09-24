package com.quorum.tessera.q2t;

import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.threading.CompletionServiceFactory;
import com.quorum.tessera.transaction.publish.BatchPayloadPublisher;
import com.quorum.tessera.transaction.publish.BatchPayloadPublisherFactory;
import com.quorum.tessera.transaction.publish.PayloadPublisher;

public class AsyncBatchPayloadPublisherFactory implements BatchPayloadPublisherFactory {

    @Override
    public BatchPayloadPublisher create(PayloadPublisher publisher) {
        CompletionServiceFactory completionServiceFactory = new CompletionServiceFactory();
        PayloadEncoder encoder = PayloadEncoder.create();
        return new AsyncBatchPayloadPublisher(completionServiceFactory, publisher, encoder);
    }
}
