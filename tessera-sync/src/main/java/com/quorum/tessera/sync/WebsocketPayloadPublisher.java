package com.quorum.tessera.sync;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.partyinfo.PayloadPublisher;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PreDestroy;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import javax.ws.rs.core.UriBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClientEndpoint(
        decoders = {SyncResponseMessageCodec.class},
        encoders = {SyncRequestMessageCodec.class})
public class WebsocketPayloadPublisher implements PayloadPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebsocketPayloadPublisher.class);

    private final WebSocketContainer container = ContainerProvider.getWebSocketContainer();

    private final Map<String, Session> cache = new ConcurrentHashMap<>();

    @Override
    public void publishPayload(EncodedPayload payload, String targetUrl) {

        LOGGER.info("Publish payload {} to target url {}", payload, targetUrl);

        URI uri = UriBuilder.fromUri(URI.create(targetUrl)).path("sync").build();

        final Session session =
                cache.computeIfAbsent(
                        targetUrl, k -> WebSocketSessionCallback.execute(() -> container.connectToServer(this, uri)));

        final SyncRequestMessage syncRequestMessage =
                SyncRequestMessage.Builder.create(SyncRequestMessage.Type.TRANSACTION_PUSH)
                        .withTransactions(payload)
                        .build();

        WebSocketSessionCallback.execute(
                () -> {
                    session.getBasicRemote().sendObject(syncRequestMessage);
                    return null;
                });
    }

    @PreDestroy
    public void clearSessions() {
        cache.values()
                .forEach(
                        s -> {
                            CloseReason reason = new CloseReason(CloseCodes.NORMAL_CLOSURE, "");
                            WebSocketSessionCallback.execute(
                                    () -> {
                                        s.close(reason);
                                        return null;
                                    });
                        });
        cache.clear();
    }
}
