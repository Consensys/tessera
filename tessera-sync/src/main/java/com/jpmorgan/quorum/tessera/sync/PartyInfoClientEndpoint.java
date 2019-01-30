package com.jpmorgan.quorum.tessera.sync;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClientEndpoint(decoders = PartyInfoCodec.class, encoders = PartyInfoCodec.class)
public class PartyInfoClientEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoClientEndpoint.class);

    @OnOpen
    public void onOpen(Session session) {
        LOGGER.info("Session id : {}", session.getId());
    }

    @OnMessage
    public void onMessage(Session session,String message) {
        LOGGER.info("Client received message: {} {}",session.getId(), message);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        LOGGER.info("Closing session : {} because {}", session.getId(), reason);
    }

}
