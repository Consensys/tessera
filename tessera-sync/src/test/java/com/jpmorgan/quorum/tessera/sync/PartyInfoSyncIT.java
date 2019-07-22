package com.jpmorgan.quorum.tessera.sync;

import com.jpmorgan.quorum.mock.servicelocator.MockServiceLocator;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.model.Party;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.model.Recipient;
import com.quorum.tessera.transaction.TransactionManager;
import java.net.URI;
import java.util.Base64;
import java.util.Collections;
import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import static org.assertj.core.api.Assertions.assertThat;
import org.glassfish.tyrus.server.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PartyInfoSyncIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoSyncIT.class);

    private PartyInfoService partyInfoService;

    private TransactionManager transactionManager;

    private Server server;

    private MockServiceLocator mockServiceLocator;

    @Before
    public void onSetUp() throws Exception {

        mockServiceLocator = MockServiceLocator.createMockServiceLocator();
        transactionManager = mock(TransactionManager.class);
        partyInfoService = mock(PartyInfoService.class);

        mockServiceLocator.setServices(Collections.singleton(partyInfoService));

        server = new Server("localhost", 8025, "/", null, PartyInfoEndpoint.class);
        server.start();
    }

    @After
    public void onTearDown() {
        server.stop();
    }

    @Test
    public void doStuff() throws Exception {

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();

        PartyInfoClientEndpoint client = new PartyInfoClientEndpoint(partyInfoService, transactionManager);

        Session clientSession = container.connectToServer(client, URI.create("ws://localhost:8025/sync"));
        LOGGER.info("Client sesssion : {}", clientSession.getId());
        PublicKey publicKey =
                PublicKey.from(Base64.getDecoder().decode("ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc="));

        PartyInfo partyInfo =
                new PartyInfo(
                        "http://bogus.com:9999",
                        Collections.singleton(new Recipient(publicKey, "http://bogus.com:9998")),
                        Collections.singleton(new Party("http://bogus.com:9997")));

        clientSession.getBasicRemote().sendObject(partyInfo);

        assertThat(server).isNotNull();
    }
}
