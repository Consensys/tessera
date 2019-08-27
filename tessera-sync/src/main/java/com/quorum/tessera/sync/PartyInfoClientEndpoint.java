package com.quorum.tessera.sync;

import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClientEndpoint(
        decoders = {SyncResponseMessageCodec.class},
        encoders = {SyncRequestMessageCodec.class})
public class PartyInfoClientEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoClientEndpoint.class);

    private final PartyInfoService partyInfoService;

    private final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());

    public PartyInfoClientEndpoint(PartyInfoService partyInfoService, Object transactionManager) {
        this(partyInfoService);
    }

    public PartyInfoClientEndpoint(PartyInfoService partyInfoService) {
        this.partyInfoService = Objects.requireNonNull(partyInfoService);
    }

    @OnOpen
    public void onOpen(Session session) {
        LOGGER.info("Open Session from  : {}", session.getRequestURI());
        sessions.add(session);
    }

    @OnMessage
    public void onResponse(Session session, SyncResponseMessage response) {

        LOGGER.info("Response.type {}", response.getType());

        if (response.getType() == SyncResponseMessage.Type.PARTY_INFO) {

            final PartyInfo partyInfo = response.getPartyInfo();

            if (Objects.nonNull(partyInfo)) {

                LOGGER.debug("Updating party info from {}", partyInfo.getUrl());

                PartyInfo updatedPartyInfo = partyInfoService.updatePartyInfo(partyInfo);

                SyncRequestMessage syncRequestMessage =
                        SyncRequestMessage.Builder.create(SyncRequestMessage.Type.PARTY_INFO)
                                .withPartyInfo(updatedPartyInfo)
                                .build();

                sessions.forEach(
                        s -> {
                            WebSocketSessionCallback.execute(
                                    () -> {
                                        LOGGER.debug("Forwarding partyinfo response to session : {}", s.getId());

                                        s.getBasicRemote().sendObject(syncRequestMessage);

                                        LOGGER.debug("Sent partyinfo response to session {}", s.getId());
                                        return null;
                                    });
                        });

                LOGGER.debug("Updated party info from {}", partyInfo.getUrl());
            }
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        sessions.remove(session);

        LOGGER.info("Closing session : {} because {}", session.getRequestURI(), reason);

        URI baseUri = UriBuilder.fromUri(session.getRequestURI()).replacePath("").build();

        partyInfoService.removeRecipient(baseUri.toString());
    }

    @OnError
    public void onError(Throwable ex) {
        LOGGER.error("", ex);
    }
}
