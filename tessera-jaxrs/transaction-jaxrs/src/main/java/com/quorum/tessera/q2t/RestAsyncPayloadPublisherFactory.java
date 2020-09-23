package com.quorum.tessera.q2t;

import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.transaction.publish.AsyncPayloadPublisher;
import com.quorum.tessera.transaction.publish.AsyncPayloadPublisherFactory;
import com.quorum.tessera.transaction.publish.PayloadPublisher;

import java.util.concurrent.CompletionService;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;

public class RestAsyncPayloadPublisherFactory implements AsyncPayloadPublisherFactory {

    @Override
    public AsyncPayloadPublisher create(PayloadPublisher publisher) {
        Executor executor = Executors.newCachedThreadPool();
        CompletionService<Void> completionService = new ExecutorCompletionService<>(executor);
        PayloadEncoder encoder = PayloadEncoder.create();
        return new RestAsyncPayloadPublisher(completionService, publisher, encoder);
    }
}
