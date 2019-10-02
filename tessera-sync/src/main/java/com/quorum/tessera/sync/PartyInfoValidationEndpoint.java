package com.quorum.tessera.sync;

import com.quorum.tessera.nacl.NaclException;
import com.quorum.tessera.partyinfo.PartyInfoService;
import java.io.IOException;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerEndpoint(value = "/validate", configurator = PartyInfoEndpointConfigurator.class)
public class PartyInfoValidationEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoValidationEndpoint.class);

    private final PartyInfoService partyInfoService;

    public PartyInfoValidationEndpoint(PartyInfoService partyInfoService) {
        this.partyInfoService = partyInfoService;
    }

    @OnMessage
    public void unencryptSampleData(Session session, byte[] data) throws IOException {
        LOGGER.debug("unencryptSampleData {}", this);
        try {
            byte[] resultsData = partyInfoService.unencryptSampleData(data);

            session.getBasicRemote().sendText(new String(resultsData));
        } catch (NaclException ex) {
            LOGGER.debug("", ex);
            session.getBasicRemote().sendText("NACK");
        }
    }

    @OnError
    public void onError(Throwable ex) {
        LOGGER.warn("{}", ex.getMessage());
        LOGGER.debug("", ex);
    }
}
