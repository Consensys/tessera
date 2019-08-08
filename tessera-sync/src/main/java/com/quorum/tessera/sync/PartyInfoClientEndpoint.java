package com.quorum.tessera.sync;

import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import java.util.Objects;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClientEndpoint(
        decoders = {SyncResponseMessageCodec.class},
        encoders = {SyncRequestMessageCodec.class})
public class PartyInfoClientEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoClientEndpoint.class);

    private final PartyInfoService partyInfoService;

    private WebSocketContainer container = ContainerProvider.getWebSocketContainer();

    public PartyInfoClientEndpoint(PartyInfoService partyInfoService, Object transactionManager) {
        this(partyInfoService);
    }

    public PartyInfoClientEndpoint(PartyInfoService partyInfoService) {
        this.partyInfoService = Objects.requireNonNull(partyInfoService);
    }

    @OnOpen
    public void onOpen(Session session) {

        LOGGER.info("Open Session  : {}", session);
    }

    @OnMessage
    public void onResponse(Session session, SyncResponseMessage response) {

        LOGGER.info("Response.type {}", response.getType());

        if (response.getType() == SyncResponseMessage.Type.PARTY_INFO) {

            final PartyInfo partyInfo = response.getPartyInfo();
            if (Objects.nonNull(partyInfo)) {
                LOGGER.info("Updating party info from {}", partyInfo.getUrl());

                partyInfoService.updatePartyInfo(partyInfo);

                LOGGER.info("Updated party info from {}", partyInfo.getUrl());
            }
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        LOGGER.info("Closing session : {} because {}", session.getId(), reason);
    }

    @OnError
    public void onError(Throwable ex) {
        LOGGER.error("", ex);
    }
}
