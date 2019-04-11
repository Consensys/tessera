package com.jpmorgan.quorum.tessera.sync;

import com.quorum.tessera.node.model.PartyInfo;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerEndpoint(value = "/sync",
        decoders = PartyInfoCodec.class,
        encoders = PartyInfoCodec.class)
public class PartyInfoEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoEndpoint.class);
    
    private final Map<String,Session> sessions = new HashMap<>();
    
    @OnOpen
    public void onOpen(Session session) {
        LOGGER.info("Open session : {}, {}",session.getId());
        sessions.put(session.getId(), session);
    } 
    
    @OnMessage
    public void onSync(Session session, PartyInfo partyInfo) {
        LOGGER.info("Message {}",partyInfo.getUrl());
        session.getAsyncRemote().sendText("ACK");
    }
    
    @OnClose
    public void onClose(Session session) {
        LOGGER.info("Close session: {}",session.getId());
        sessions.remove(session.getId());
    } 

    public Collection<Session> getSessions() {
        return Collections.unmodifiableCollection(sessions.values());
    }
    
}
