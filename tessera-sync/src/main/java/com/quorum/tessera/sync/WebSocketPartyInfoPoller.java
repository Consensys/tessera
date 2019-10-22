package com.quorum.tessera.sync;

import com.quorum.tessera.partyinfo.PartyInfoPoller;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.model.Party;
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

public class WebSocketPartyInfoPoller implements PartyInfoPoller {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketPartyInfoPoller.class);

    private final PartyInfoService partyInfoService;

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public WebSocketPartyInfoPoller(PartyInfoService partyInfoService) {
        this.partyInfoService = partyInfoService;
    }

    @Override
    public void run() {

        final WebSocketContainer container = ContainerProvider.getWebSocketContainer();

        final PartyInfo partyInfo = partyInfoService.getPartyInfo();

        final Queue<Party> partyQueue = new LinkedList<>(partyInfo.getParties());

        Party party = null;

        while ((party = partyQueue.poll()) != null) {

            UriBuilder uriBuilder = UriBuilder.fromUri(URI.create(party.getUrl())).path("sync");

            final PartyInfoClientEndpoint endpoint = new PartyInfoClientEndpoint(partyInfoService);

            URI uri = uriBuilder.build();

            SyncRequestMessage syncRequestMessage =
                    SyncRequestMessage.Builder.create(SyncRequestMessage.Type.PARTY_INFO)
                            .withPartyInfo(partyInfo)
                            .build();

            try {
                Session session =
                        sessions.computeIfAbsent(
                                party.getUrl(),
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
                sessions.remove(party.getUrl());
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
