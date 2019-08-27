package com.quorum.tessera.sync;

import com.jpmorgan.quorum.mock.servicelocator.MockServiceLocator;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.model.Party;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.model.Recipient;
import com.quorum.tessera.transaction.TransactionManager;
import java.io.IOException;
import java.net.URI;
import java.util.Base64;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import org.glassfish.tyrus.server.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PartyInfoSyncIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoSyncIT.class);

    private PartyInfoService partyInfoService;

    private TransactionManager transactionManager;

    private Server server;

    private MockServiceLocator mockServiceLocator;

    private Enclave enclave;

    private PartyInfoClientEndpoint client;

    private WebSocketContainer container = ContainerProvider.getWebSocketContainer();

    private Session clientSession;

    @Before
    public void onSetUp() throws Exception {

        mockServiceLocator = MockServiceLocator.createMockServiceLocator();
        transactionManager = mock(TransactionManager.class);
        partyInfoService = mock(PartyInfoService.class);
        enclave = mock(Enclave.class);

        mockServiceLocator.setServices(
                Stream.of(transactionManager, partyInfoService, enclave).collect(Collectors.toSet()));

        server = new Server("localhost", 8025, "/", null, PartyInfoEndpoint.class);
        server.start();

        client = new PartyInfoClientEndpoint(partyInfoService, transactionManager);
        clientSession = container.connectToServer(client, URI.create("ws://localhost:8025/sync"));
    }

    @After
    public void onTearDown() throws IOException {
        // server.stop();
        clientSession.close();
    }

    @Test
    public void doStuff() throws Exception {

        LOGGER.info("Client sesssion : {}", clientSession.getId());
        PublicKey publicKey =
                PublicKey.from(Base64.getDecoder().decode("ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc="));

        PartyInfo partyInfo =
                new PartyInfo(
                        "http://bogus.com:9999",
                        Collections.singleton(new Recipient(publicKey, "http://bogus.com:9998")),
                        Collections.singleton(new Party("http://bogus.com:9997")));

        when(partyInfoService.updatePartyInfo(any(PartyInfo.class))).thenReturn(partyInfo);
        when(partyInfoService.getPartyInfo()).thenReturn(partyInfo);

        SyncRequestMessage requestMessage =
                SyncRequestMessage.Builder.create(SyncRequestMessage.Type.PARTY_INFO).withPartyInfo(partyInfo).build();

        LOGGER.info("Sending");
        clientSession.getBasicRemote().sendObject(requestMessage);
        LOGGER.info("Sent");
    }
}
