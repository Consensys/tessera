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
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.tyrus.server.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Ignore
public class PartyInfoSyncIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoSyncIT.class);

    private PartyInfoService partyInfoService;

    private TransactionManager transactionManager;

    private MockServiceLocator mockServiceLocator;

    private Enclave enclave;

    private final WebSocketContainer container = ContainerProvider.getWebSocketContainer();

    private List<Session> sessions = new ArrayList<>();

    private List<Server> servers;

    @Before
    public void onSetUp() throws Exception {

        mockServiceLocator = MockServiceLocator.createMockServiceLocator();
        transactionManager = mock(TransactionManager.class);

        partyInfoService = mock(PartyInfoService.class);
        enclave = mock(Enclave.class);

        mockServiceLocator.setServices(
                Stream.of(transactionManager, partyInfoService, enclave).collect(Collectors.toSet()));

        this.servers =
                IntStream.range(8025, 8029)
                        .mapToObj(port -> new Server("localhost", port, "/", null, PartyInfoEndpoint.class))
                        .collect(Collectors.toList());

        servers.stream()
                .forEach(
                        s -> {
                            try {
                                s.start();
                            } catch (DeploymentException ex) {
                                throw new RuntimeException(ex);
                            }
                        });
    }

    @After
    public void onTearDown() throws IOException {

        sessions.forEach(
                s -> {
                    try {
                        s.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "BYE"));
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                });

        servers.forEach(Server::stop);
    }

    @Test
    public void doStuff() throws Exception {

        PublicKey publicKey =
                PublicKey.from(Base64.getDecoder().decode("ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc="));

        Map<Integer, PublicKey> portKeyPairs = new HashMap<>();
        portKeyPairs.put(8025, publicKey);
        portKeyPairs.put(8026, publicKey);
        portKeyPairs.put(8027, publicKey);

        Set<Party> parties =
                IntStream.range(8025, 8029)
                        .mapToObj(port -> String.format("ws://localhost:%S/sync", port))
                        .map(Party::new)
                        .collect(Collectors.toSet());

        PartyInfo partyInfo =
                new PartyInfo(
                        "ws://localhost:8025/sync",
                        Collections.singleton(new Recipient(publicKey, "ws://localhost:8026/sync")),
                        Collections.EMPTY_SET);

        PartyInfo partyInfo2 =
                new PartyInfo(
                        "ws://localhost:8026/sync",
                        Collections.singleton(new Recipient(publicKey, "ws://localhost:8027/sync")),
                        Collections.EMPTY_SET);

        PartyInfo partyInfo3 =
                new PartyInfo(
                        "ws://localhost:8027/sync",
                        Collections.singleton(new Recipient(publicKey, "ws://localhost:8028/sync")),
                        Collections.EMPTY_SET);

        PartyInfo partyInfo4 =
                new PartyInfo(
                        "ws://localhost:8028/sync",
                        Collections.singleton(new Recipient(publicKey, "ws://localhost:8025/sync")),
                        Collections.EMPTY_SET);

        when(partyInfoService.updatePartyInfo(any(PartyInfo.class)))
                .thenReturn(partyInfo, partyInfo2, partyInfo3, partyInfo4);

        when(partyInfoService.getPartyInfo()).thenReturn(partyInfo, partyInfo2, partyInfo3, partyInfo4);

        SyncRequestMessage requestMessage =
                SyncRequestMessage.Builder.create(SyncRequestMessage.Type.PARTY_INFO).withPartyInfo(partyInfo).build();

        LOGGER.info("Sending");

        for (int port = 8025; port < 8029; port++) {

            PartyInfoClientEndpoint client = new PartyInfoClientEndpoint(partyInfoService, transactionManager);
            URI uri = UriBuilder.fromPath("/sync").port(port).host("localhost").scheme("ws").build();

            Session session = container.connectToServer(client, uri);

            session.getBasicRemote().sendObject(requestMessage);

            sessions.add(session);
            LOGGER.info("Sent requestMessage to {}", uri);
        }

        System.in.read();
    }
}
