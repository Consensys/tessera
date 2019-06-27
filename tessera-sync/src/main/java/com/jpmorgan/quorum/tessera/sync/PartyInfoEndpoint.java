package com.jpmorgan.quorum.tessera.sync;

import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerEndpoint(
    value = "/sync",
    decoders = PartyInfoCodec.class,
    encoders = PartyInfoCodec.class,
    configurator = PartyInfoEndpointConfigurator.class)
public class PartyInfoEndpoint {

  private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoEndpoint.class);

  private final Map<String, Session> sessions = new HashMap<>();

  private final PartyInfoService partyInfoService;

  public PartyInfoEndpoint(PartyInfoService partyInfoService) {
    this.partyInfoService = partyInfoService;
  }

  @OnOpen
  public void onOpen(Session session) {
    LOGGER.info("Open session : {}, {}", session.getId());
    sessions.put(session.getId(), session);
  }

  @OnMessage
  public PartyInfo onSync(Session session, PartyInfo partyInfo) throws IOException, EncodeException {
    LOGGER.info("Message {}", partyInfo.getUrl());

    PartyInfo mergedPartyInfo = partyInfoService.updatePartyInfo(partyInfo);

    return mergedPartyInfo;
  }

  @OnClose
  public void onClose(Session session) {
    partyInfoService.removeRecipient(session.getRequestURI().toString());
    LOGGER.info("Close session: {}", session.getId());
    sessions.remove(session.getId());
  }

  public Collection<Session> getSessions() {
    return Collections.unmodifiableCollection(sessions.values());
  }
}
