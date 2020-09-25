package com.quorum.tessera.q2t;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.encryption.KeyNotFoundException;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.threading.CompletionServiceFactory;
import com.quorum.tessera.transaction.publish.BatchPayloadPublisher;
import com.quorum.tessera.transaction.publish.BatchPublishPayloadException;
import com.quorum.tessera.transaction.publish.PayloadPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AsyncBatchPayloadPublisher implements BatchPayloadPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncBatchPayloadPublisher.class);

    private final Executor executor = Executors.newCachedThreadPool();

    private final CompletionServiceFactory completionServiceFactory;

    private final PayloadPublisher publisher;

    private final PayloadEncoder encoder;

    public AsyncBatchPayloadPublisher(CompletionServiceFactory completionServiceFactory, PayloadPublisher publisher, PayloadEncoder encoder) {
        this.completionServiceFactory = completionServiceFactory;
        this.publisher = publisher;
        this.encoder = encoder;
    }

    /**
     * Asynchronously strips (leaving data intended only for that particular recipient) and publishes the payload to
     * each recipient identified by the provided keys.
     *
     * <p>This method blocks until all pushes return successfully; if a push fails with an exception, the method exits
     * immediately and does not wait for the remaining responses.
     *
     * @param payload the payload object to be stripped and pushed
     * @param recipientKeys list of public keys identifying the target nodes
     */
    @Override
    public void publishPayload(EncodedPayload payload, List<PublicKey> recipientKeys) {
        final CompletionService<Void> completionService = completionServiceFactory.create(executor);

        recipientKeys.forEach(
            recipient -> completionService.submit(() -> {
                final EncodedPayload outgoing = encoder.forRecipient(payload, recipient);
                publisher.publishPayload(outgoing, recipient);
                return null;
            })
        );

        int awaitingCompletionCount = recipientKeys.size();

        try {
            while (awaitingCompletionCount > 0) {
                awaitingCompletionCount--;
                waitForNextCompletion(completionService);
            }
        } catch (Exception e) {
            LOGGER.debug("Cleaning up after async payload publish exited early");
            long count = awaitingCompletionCount;
            executor.execute(() -> waitForMultipleCompletions(completionService, count));
            throw e;
        }
    }

    /**
     * Waits until a task submitted to the CompletionService completes.
     *
     * <p>If an {@link ExecutionException} or {@link InterruptedException} is encountered, the method throws an
     * exception and exits without waiting for the remaining tasks to complete.
     *
     * <p>To prevent a memory leak, the caller should remove the remaining tasks from the CompletionService after this
     * method exits.
     *
     * @param completionService a service with submitted tasks
     */
    private void waitForNextCompletion(CompletionService<Void> completionService) {
        try {
            LOGGER.debug("waiting for task to complete...");
            completionService.take().get();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof KeyNotFoundException) {
                throw (KeyNotFoundException) cause;
            }
            throw new BatchPublishPayloadException(cause);
        } catch (InterruptedException | RuntimeException e) {
            throw new BatchPublishPayloadException(e);
        }
    }

    /**
     * Removes count tasks from the CompletionService, waiting for each to complete in turn.
     *
     * <p>If an exception is encountered, it is ignored so that the remaining tasks can be removed.
     *
     * @param completionService a service with submitted tasks
     * @param count the number of tasks to drain from the completionService
     */
    private void waitForMultipleCompletions(CompletionService<Void> completionService, long count) {
        long n = count;
        while (n > 0) {
            n--;
            try {
                waitForNextCompletion(completionService);
            } catch (Exception e) {
                LOGGER.debug("Unable to drain task from CompletionService: {}", e.getMessage());
            }
        }
    }
}
