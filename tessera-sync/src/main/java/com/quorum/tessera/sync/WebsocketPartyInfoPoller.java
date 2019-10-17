package com.quorum.tessera.sync;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.Peer;
import com.quorum.tessera.partyinfo.PartyInfoPoller;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.Map;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PreDestroy;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import javax.ws.rs.core.UriBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebsocketPartyInfoPoller implements PartyInfoPoller {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebsocketPartyInfoPoller.class);

    private final PartyInfoService partyInfoService;

    private final Config config;

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public WebsocketPartyInfoPoller(Config config, PartyInfoService partyInfoService) {
        this.partyInfoService = partyInfoService;
        this.config = config;
    }

    @Override
    public void run() {

        final WebSocketContainer container = ContainerProvider.getWebSocketContainer();

        final Queue<Peer> peerQueue = new LinkedList<>(config.getPeers());

        Peer peer = null;

        while ((peer = peerQueue.poll()) != null) {

            UriBuilder uriBuilder = UriBuilder.fromUri(URI.create(peer.getUrl())).path("sync");

            final PartyInfoClientEndpoint endpoint = new PartyInfoClientEndpoint(partyInfoService);

            PartyInfo partyInfo = partyInfoService.getPartyInfo();

            URI uri = uriBuilder.build();

            SyncRequestMessage syncRequestMessage =
                    SyncRequestMessage.Builder.create(SyncRequestMessage.Type.PARTY_INFO)
                            .withPartyInfo(partyInfo)
                            .build();

            try {
                Session session =
                        sessions.computeIfAbsent(
                                peer.getUrl(),
                                k -> WebSocketSessionCallback.execute(() -> container.connectToServer(endpoint, uri)));
                LOGGER.debug("Connecting to server {}", uri);

                WebSocketSessionCallback.execute(
                        () -> {
                            session.getBasicRemote().sendObject(syncRequestMessage);
                            return null;
                        });

            } catch (UncheckedIOException | UncheckedWebSocketException ex) {
                LOGGER.warn("Exception while polling party info from {}. Exception message {}", uri, ex.getMessage());
                LOGGER.debug("", ex);
                sessions.remove(peer.getUrl());
            }
        }
    }

    @PreDestroy
    public void clearSessions() {
        sessions.values()
                .forEach(
                        s -> {
                            CloseReason reason = new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "");
                            WebSocketSessionCallback.execute(
                                    () -> {
                                        s.close(reason);
                                        return null;
                                    });
                        });
        sessions.clear();
    }
}
