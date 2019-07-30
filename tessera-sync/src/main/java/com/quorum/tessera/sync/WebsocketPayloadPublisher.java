package com.quorum.tessera.sync;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.partyinfo.PayloadPublisher;
import java.net.URI;
import javax.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebsocketPayloadPublisher implements PayloadPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebsocketPayloadPublisher.class);

    private final SessionStore sessionStore;
    
    public WebsocketPayloadPublisher(SessionStore sessionStore) {
        this.sessionStore = sessionStore;
        LOGGER.info("Created instance {}",this);
    }

    @Override
    public void publishPayload(EncodedPayload payload, String targetUrl) {

        final URI uri = URI.create(targetUrl);

        final Session session = sessionStore.findByUri(uri);

        final SyncRequestMessage syncRequestMessage = SyncRequestMessage.Builder
                .create(SyncRequestMessage.Type.TRANSACTION_PUSH)
                .withTransactions(payload)
                .build();

        WebSocketSessionCallback.execute(() -> {
            session.getBasicRemote().sendObject(syncRequestMessage);
            return null;
        });

    }

}
