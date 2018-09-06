package com.quorum.tessera.sync;

import com.quorum.tessera.api.model.ResendRequest;
import com.quorum.tessera.api.model.ResendRequestType;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.key.KeyManager;
import com.quorum.tessera.nacl.Key;
import com.quorum.tessera.node.PostDelegate;
import com.quorum.tessera.node.grpc.GrpcClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class TransactionRequesterImpl implements TransactionRequester {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionRequesterImpl.class);

    private final KeyManager keyManager;

    private final PostDelegate client;

    private final CommunicationType communicationType;

    public TransactionRequesterImpl(final KeyManager keyManager,
                                    final PostDelegate client,
                                    final CommunicationType communicationType) {
        this.keyManager = Objects.requireNonNull(keyManager);
        this.client = Objects.requireNonNull(client);
        this.communicationType = communicationType;
    }

    @Override
    public boolean requestAllTransactionsFromNode(final String uri) {

        LOGGER.debug("Requesting transactions get resent for {}", uri);

        return this.keyManager
            .getPublicKeys()
            .parallelStream()
            .map(this::createRequestAllEntity)
            .allMatch(req -> this.makeRequest(uri, req));

    }

    /**
     * Will make the desired request until succeeds or max tries has been reached
     *
     * @param uri     the URI to call
     * @param request the request object to send
     */
    private boolean makeRequest(final String uri, final ResendRequest request) {
        LOGGER.debug("Requesting a resend for key {}", request.getPublicKey());

        boolean success;
        int numberOfTries = 0;

        do {
            try {
                if (communicationType == CommunicationType.GRPC) {
                    com.quorum.tessera.api.grpc.model.ResendRequest gRPCRequest =
                        com.quorum.tessera.api.grpc.model.ResendRequest.newBuilder()
                            .setType(
                                request.getType() == ResendRequestType.ALL ?
                                    com.quorum.tessera.api.grpc.model.ResendRequestType.ALL :
                                    com.quorum.tessera.api.grpc.model.ResendRequestType.INDIVIDUAL
                            )
                            .setKey(request.getKey())
                            .setPublicKey(request.getPublicKey())
                            .build();
                    success = GrpcClientFactory.getClient(uri).makeResendRequest(gRPCRequest);
                } else {
                    success = this.client.makeResendRequest(uri, request);
                }
            } catch (final Exception ex) {
                success = false;
                LOGGER.debug("Failed to make resend request to node {} for key {}", uri, request.getPublicKey());
            }

            numberOfTries++;

        } while (!success && (numberOfTries < MAX_ATTEMPTS));

        return success;

    }

    /**
     * Creates the entity that should be sent to the target URL
     *
     * @param key the public key that transactions should be resent for
     * @return the request to be sent
     */
    private ResendRequest createRequestAllEntity(final Key key) {

        final ResendRequest request = new ResendRequest();
        request.setPublicKey(key.toString());
        request.setType(ResendRequestType.ALL);

        return request;
    }

}
