package com.jpmorgan.quorum.tessera.sync;

import com.quorum.tessera.core.api.ServiceFactory;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import java.net.URI;
import java.util.Objects;
import javax.websocket.ClientEndpoint;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClientEndpoint(
        decoders = {SyncRequestMessageCodec.class, SyncResponseMessageCodec.class},
        encoders = {SyncRequestMessageCodec.class, SyncResponseMessageCodec.class})
public class PartyInfoClientEndpoint extends ClientEndpointConfig.Configurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoClientEndpoint.class);

    private final PartyInfoService partyInfoService;

    public PartyInfoClientEndpoint() {
        this(ServiceFactory.create().partyInfoService());
    }

    public PartyInfoClientEndpoint(PartyInfoService partyInfoService) {
        this.partyInfoService = Objects.requireNonNull(partyInfoService);
    }

    @OnOpen
    public void onOpen(Session session) {
        LOGGER.info("Session id : {}", session.getId());
    }

    @OnMessage
    public void onMessage(Session session, PartyInfo partyInfo) {

        LOGGER.info("Client received message: {} {}", session.getId(), partyInfo);

        PartyInfo existingPartyInfo = partyInfoService.getPartyInfo();

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();

        partyInfo.getRecipients().stream()
                .filter(r -> !existingPartyInfo.getRecipients().contains(r))
                .map(r -> r.getUrl())
                .map(URI::create)
                .forEach(
                        u -> {
                            WebSocketSessionCallback.execute(
                                    () -> {
                                        Session s = container.connectToServer(this, u);
                                        s.getBasicRemote().sendObject(partyInfo);
                                        return null;
                                    });
                        });
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        LOGGER.info("Closing session : {} because {}", session.getId(), reason);
    }
}
