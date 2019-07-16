package com.jpmorgan.quorum.tessera.sync;

import com.quorum.tessera.core.api.ServiceFactory;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.transaction.PayloadPublisher;
import com.quorum.tessera.transaction.TransactionManager;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

public class TransactionPublisher implements PayloadPublisher {

    private final PartyInfoService partyInfoService;

    private final ConcurrentMap<PublicKey, List<Session>> sessions = new ConcurrentHashMap<>();

    private final PartyInfoClientEndpoint partyInfoClientEndpoint;

    public TransactionPublisher() {
        this(ServiceFactory.create());
    }

    public TransactionPublisher(ServiceFactory serviceFactory) {
        this(serviceFactory.partyInfoService(), serviceFactory.transactionManager());
    }

    public TransactionPublisher(PartyInfoService partyInfoService, PartyInfoClientEndpoint partyInfoClientEndpoint) {
        this.partyInfoService = Objects.requireNonNull(partyInfoService);
        this.partyInfoClientEndpoint = Objects.requireNonNull(partyInfoClientEndpoint);
    }

    public TransactionPublisher(PartyInfoService partyInfoService, TransactionManager transactionManager) {
        this(partyInfoService, new PartyInfoClientEndpoint(partyInfoService, transactionManager));
    }

    @Override
    public void publishPayload(EncodedPayload payload, PublicKey recipientKey) {

        SyncRequestMessage message =
                SyncRequestMessage.Builder.create(SyncRequestMessage.Type.TRANSACTION_PUSH)
                        .withTransactions(payload)
                        .build();

        WebSocketSessionCallback.execute(
                () -> {
                    List<Session> session = sessions.computeIfAbsent(recipientKey, k -> createSession(k));
                    for (Session s : session) {
                        s.getBasicRemote().sendObject(message);
                    }
                    return null;
                });
    }

    private List<Session> createSession(PublicKey recipientKey) {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();

        return partyInfoService.getUrlsForKey(recipientKey).stream()
                .map(URI::create)
                .map(
                        u -> {
                            return WebSocketSessionCallback.execute(
                                    () -> {
                                        return container.connectToServer(partyInfoClientEndpoint, u);
                                    });
                        })
                .collect(Collectors.toList());
    }
}
