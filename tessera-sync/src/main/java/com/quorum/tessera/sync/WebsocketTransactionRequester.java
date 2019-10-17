package com.quorum.tessera.sync;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.transaction.TransactionRequester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;

@ClientEndpoint(encoders = {SyncRequestMessageCodec.class})
public class WebsocketTransactionRequester implements TransactionRequester {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebsocketTransactionRequester.class);

    private final Enclave enclave;

    public WebsocketTransactionRequester(Enclave enclave) {
        this.enclave = enclave;
    }

    @Override
    public boolean requestAllTransactionsFromNode(String url) {
        LOGGER.debug("Requesting transactions get resent for {}", url);

        return this.enclave.getPublicKeys().stream()
            .map(this::createRequestAllEntity)
            .allMatch(req -> this.makeRequest(url, req));
    }

    /**
     * Will make the desired request until succeeds or max tries has been reached
     *
     * @param url the URL to call
     * @param request the request object to send
     */
    private boolean makeRequest(final String url, final SyncRequestMessage request) {
        LOGGER.debug("Requesting a resend for key {}", request.getRecipientKey());

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();

        boolean success;
        int numberOfTries = 0;

        final URI uri = UriBuilder.fromUri(URI.create(url)).path("sync").build();

        do {
            try {
                final Session session = container.connectToServer(this, uri);
                LOGGER.debug("Connecting to server {}", uri);

                WebSocketSessionCallback.execute(
                    () -> {
                        session.getBasicRemote().sendObject(request);
                        return null;
                    });
                success = true;
            } catch (UncheckedWebSocketException | DeploymentException | IOException ex) {
                success = false;
                LOGGER.error("Excepting while sending resend request to {}. Exception message {}", uri, ex.getMessage());
                LOGGER.error("", ex);
            }

        } while (!success && (numberOfTries < MAX_ATTEMPTS));

        return success;
    }

    /**
     * Creates the entity that should be sent to the target URL
     *
     * @param key the public key that transactions should be resent for
     * @return the request to be sent
     */
    private SyncRequestMessage createRequestAllEntity(final PublicKey key) {
        return SyncRequestMessage.Builder.create(SyncRequestMessage.Type.TRANSACTION_SYNC)
                .withRecipientKey(key)
                .build();
    }
}
