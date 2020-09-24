package com.quorum.tessera.q2t;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.encryption.KeyNotFoundException;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.transaction.publish.BatchPayloadPublisher;
import com.quorum.tessera.transaction.publish.BatchPublishPayloadException;
import com.quorum.tessera.transaction.publish.PayloadPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class AsyncBatchPayloadPublisher implements BatchPayloadPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncBatchPayloadPublisher.class);

    private final CompletionService<Void> completionService;

    private final PayloadPublisher publisher;

    private final PayloadEncoder encoder;

    public AsyncBatchPayloadPublisher(CompletionService<Void> completionService, PayloadPublisher publisher, PayloadEncoder encoder) {
        this.completionService = completionService;
        this.publisher = publisher;
        this.encoder = encoder;
    }

    /**
     * Asynchronously strips (leaving data intended only for that particular recipient) and publishes the payload to
     * each recipient identified by the provided keys.
     * This method blocks until all pushes return successfully; if a push fails with an exception, the method exits
     * immediately and does not wait for the remaining responses.
     *
     * @param payload the payload object to be stripped and pushed
     * @param recipientKeys list of public keys identifying the target nodes
     */
    @Override
    public void publishPayload(EncodedPayload payload, List<PublicKey> recipientKeys) {
        // asynchronously submit all publishes
        List<Future<Void>> futures =
                recipientKeys.stream()
                        .map(
                                recipient ->
                                        completionService.submit(
                                                () -> {
                                                    final EncodedPayload outgoing =
                                                            encoder.forRecipient(payload, recipient);
                                                    publisher.publishPayload(outgoing, recipient);
                                                    return null;
                                                }))
                        .collect(Collectors.toList());

        // wait for publishes to complete, exiting if at least one returns an error
        for (int i = 0; i < futures.size(); i++) {
            try {
                completionService.take().get();
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                LOGGER.debug("Unable to publish payload, exiting async publish", cause);
                if (cause instanceof KeyNotFoundException) {
                    throw (KeyNotFoundException) cause;
                }
                throw new BatchPublishPayloadException(cause);
            } catch (InterruptedException e) {
                LOGGER.debug("Async payload publish interrupted", e);
                throw new BatchPublishPayloadException(e);
            }
        }
    }
}
