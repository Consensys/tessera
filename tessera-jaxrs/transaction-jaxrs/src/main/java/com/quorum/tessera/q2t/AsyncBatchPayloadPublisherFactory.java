package com.quorum.tessera.q2t;

import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.transaction.publish.BatchPayloadPublisher;
import com.quorum.tessera.transaction.publish.BatchPayloadPublisherFactory;
import com.quorum.tessera.transaction.publish.PayloadPublisher;

import java.util.concurrent.CompletionService;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;

public class AsyncBatchPayloadPublisherFactory implements BatchPayloadPublisherFactory {

    @Override
    public BatchPayloadPublisher create(PayloadPublisher publisher) {
        Executor executor = Executors.newCachedThreadPool();
        CompletionService<Void> completionService = new ExecutorCompletionService<>(executor);
        PayloadEncoder encoder = PayloadEncoder.create();
        return new AsyncBatchPayloadPublisher(completionService, publisher, encoder);
    }
}
