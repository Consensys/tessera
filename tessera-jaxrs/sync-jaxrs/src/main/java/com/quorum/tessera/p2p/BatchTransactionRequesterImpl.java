package com.quorum.tessera.p2p;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.p2p.ResendClient;
import com.quorum.tessera.partyinfo.ResendBatchRequest;
import com.quorum.tessera.partyinfo.ResendBatchResponse;
import com.quorum.tessera.partyinfo.TransactionRequester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.Objects;

public class BatchTransactionRequesterImpl implements TransactionRequester {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchTransactionRequesterImpl.class);

    private final Enclave enclave;

    private final ResendClient client;

    private final int batchSize;

    public BatchTransactionRequesterImpl(final Enclave enclave, final ResendClient client, int batchSize) {
        this.enclave = Objects.requireNonNull(enclave);
        this.client = Objects.requireNonNull(client);
        this.batchSize = batchSize;
    }

    @Override
    public boolean requestAllTransactionsFromNode(final String uri) {

        LOGGER.info("Requesting transactions get resent for {}", uri);

        return this.enclave.getPublicKeys().stream()
                .map(this::createRequestAllEntity)
                .allMatch(req -> this.makeRequest(uri, req) >= 0);
    }

    /**
     * Will make the desired request until succeeds or max tries has been reached
     *
     * @param uri the URI to call
     * @param request the request object to send
     */
    private long makeRequest(final String uri, final ResendBatchRequest request) {
        LOGGER.debug("Requesting a batch resend for key {}", request.getPublicKey());

        ResendBatchResponse response = null;
        int numberOfTries = 0;

        do {

            try {

                response = client.makeBatchResendRequest(uri, request);
            } catch (final Exception ex) {
                LOGGER.debug("Failed to make batch resend request to node {} for key {}", uri, request.getPublicKey());
            }

            numberOfTries++;

        } while ((null == response) && (numberOfTries < MAX_ATTEMPTS));

        return response != null ? response.getTotal() : -1;
    }

    /**
     * Creates the entity that should be sent to the target URL
     *
     * @param key the public key that transactions should be resent for
     * @return the request to be sent
     */
    private ResendBatchRequest createRequestAllEntity(final PublicKey key) {

        final ResendBatchRequest request = new ResendBatchRequest();
        String encoded = Base64.getEncoder().encodeToString(key.getKeyBytes());
        request.setPublicKey(encoded);
        request.setBatchSize(batchSize);

        return request;
    }
}
