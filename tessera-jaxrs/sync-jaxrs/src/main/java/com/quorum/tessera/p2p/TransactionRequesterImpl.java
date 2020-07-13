package com.quorum.tessera.p2p;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.sync.ResendClient;
import com.quorum.tessera.sync.ResendClientFactory;
import com.quorum.tessera.sync.TransactionRequester;
import com.quorum.tessera.sync.TransactionRequesterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class TransactionRequesterImpl implements TransactionRequester, TransactionRequesterFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionRequesterImpl.class);

    private final Enclave enclave;

    private final ResendClient client;

    public TransactionRequesterImpl(final Enclave enclave, final ResendClient client) {
        this.enclave = Objects.requireNonNull(enclave);
        this.client = Objects.requireNonNull(client);
    }

    @Override
    public boolean requestAllTransactionsFromNode(final String uri) {
        return this.enclave.getPublicKeys().stream()
                .map(this::createRequestAllEntity)
                .allMatch(req -> this.makeRequest(uri, req));
    }

    /**
     * Will make the desired request until succeeds or max tries has been reached
     *
     * @param uri the URI to call
     * @param request the request object to send
     */
    private boolean makeRequest(final String uri, final ResendRequest request) {
        LOGGER.debug("Requesting a resend to {} for key {}", uri, request.getPublicKey());

        try {
            return client.makeResendRequest(uri, request);
        } catch (final Exception ex) {
            LOGGER.warn(
                    "Failed to make resend request to node {} for key {}, due to {}",
                    uri,
                    request.getPublicKey(),
                    ex.getMessage());
            return false;
        }
    }

    /**
     * Creates the entity that should be sent to the target URL
     *
     * @param key the public key that transactions should be resent for
     * @return the request to be sent
     */
    private ResendRequest createRequestAllEntity(final PublicKey key) {

        final ResendRequest request = new ResendRequest();
        final String encoded = key.encodeToBase64();
        request.setPublicKey(encoded);
        request.setType("ALL");

        return request;
    }


    public TransactionRequester createTransactionRequester(Config config) {
        Enclave enclave = EnclaveFactory.create().create(config);
        ResendClient resendClient = ResendClientFactory.newFactory(config).create(config);
        return new TransactionRequesterImpl(enclave,resendClient);
    }
}
