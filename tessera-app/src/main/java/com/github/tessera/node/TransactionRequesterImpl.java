package com.github.tessera.node;

import com.github.tessera.api.model.ResendRequest;
import com.github.tessera.api.model.ResendRequestType;
import com.github.tessera.key.KeyManager;
import com.github.tessera.nacl.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Objects;

public class TransactionRequesterImpl implements TransactionRequester {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionRequesterImpl.class);

    private static final int MAX_ATTEMPTS = 5;

    private final KeyManager keyManager;

    private PostDelegate client;

    public TransactionRequesterImpl(final KeyManager keyManager,
                                    final PostDelegate client) {
        this.keyManager = Objects.requireNonNull(keyManager);
        this.client = Objects.requireNonNull(client);
    }

    @Override
    public void requestAllTransactionsFromNode(final Collection<String> uris) {

        LOGGER.info("Requesting transactions get resent for {}", uris);

        this.keyManager
            .getPublicKeys()
            .parallelStream()
            .forEach(key -> {
                    LOGGER.debug("Requesting a resend for key {}", key);

                    uris
                        .parallelStream()
                        .forEach(uri -> this.makeRequest(uri, createRequestEntity(key)));
                }
            );

    }

    /**
     * Will make the desired request until succeeds or max tries has been reached
     *
     * @param uri     the URI to call
     * @param request the request object to send
     */
    private void makeRequest(final String uri, final ResendRequest request) {

        boolean success;
        int numberOfTries = 0;

        do {
            try {
                success = this.client.makeResendRequest(uri, request);
            } catch (final Exception ex) {
                success = false;
                LOGGER.error("Failed to make resend request to node {} for key {}", uri, request.getPublicKey());
            }

            numberOfTries++;

        } while (!success && (numberOfTries < MAX_ATTEMPTS));

    }

    private ResendRequest createRequestEntity(final Key key) {

        final ResendRequest request = new ResendRequest();
        request.setPublicKey(key.toString());
        request.setType(ResendRequestType.ALL);

        return request;
    }

}
