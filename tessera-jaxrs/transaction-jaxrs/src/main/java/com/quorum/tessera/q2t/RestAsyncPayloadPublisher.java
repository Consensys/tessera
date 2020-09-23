package com.quorum.tessera.q2t;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.encryption.KeyNotFoundException;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.transaction.publish.AsyncPayloadPublisher;
import com.quorum.tessera.transaction.publish.AsyncPublishPayloadException;
import com.quorum.tessera.transaction.publish.PayloadPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class RestAsyncPayloadPublisher implements AsyncPayloadPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestAsyncPayloadPublisher.class);

    private final CompletionService<Void> completionService;

    private final PayloadPublisher publisher;

    private final PayloadEncoder encoder;

    public RestAsyncPayloadPublisher(
            CompletionService<Void> completionService, PayloadPublisher publisher, PayloadEncoder encoder) {
        this.completionService = completionService;
        this.publisher = publisher;
        this.encoder = encoder;
    }

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
                throw new AsyncPublishPayloadException(cause);
            } catch (InterruptedException e) {
                LOGGER.debug("Async payload publish interrupted", e);
                throw new AsyncPublishPayloadException(e);
            }
        }
    }
}
