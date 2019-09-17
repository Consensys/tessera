package com.quorum.tessera.sync;

import com.quorum.tessera.partyinfo.PartyInfoService;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.EncodeException;
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

    private final Set<Session> sessions = ConcurrentHashMap.newKeySet();

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
    public void onResponse(Session session, SyncResponseMessage response) throws IOException, EncodeException {
        LOGGER.info("Process response from {}", session.getRequestURI());
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
        LOGGER.error("Handle error", ex);
    }
}
