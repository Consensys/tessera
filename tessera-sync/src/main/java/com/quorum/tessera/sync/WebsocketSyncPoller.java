package com.quorum.tessera.sync;

import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.model.Party;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.transaction.ResendPartyStore;
import com.quorum.tessera.transaction.SyncPoller;
import com.quorum.tessera.transaction.SyncableParty;
import com.quorum.tessera.transaction.TransactionRequester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class WebsocketSyncPoller implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SyncPoller.class);

    private final ExecutorService executorService;

    private final ResendPartyStore resendPartyStore;

    private final TransactionRequester transactionRequester;

    private final PartyInfoService partyInfoService;

    public WebsocketSyncPoller(
        ResendPartyStore resendPartyStore,
        TransactionRequester transactionRequester,
        PartyInfoService partyInfoService) {

        this(
            Executors.newCachedThreadPool(),
            resendPartyStore,
            transactionRequester,
            partyInfoService);
    }

    public WebsocketSyncPoller(
        final ExecutorService executorService,
        final ResendPartyStore resendPartyStore,
        final TransactionRequester transactionRequester,
        final PartyInfoService partyInfoService) {
        this.executorService = Objects.requireNonNull(executorService);
        this.resendPartyStore = Objects.requireNonNull(resendPartyStore);
        this.transactionRequester = Objects.requireNonNull(transactionRequester);
        this.partyInfoService = Objects.requireNonNull(partyInfoService);
    }

    /**
     * Retrieves all of the outstanding parties and makes an attempt to make the resend request asynchronously. If the
     * request fails then the party is submitted back to the store for a later attempt.
     */
    @Override
    public void run() {

        final PartyInfo partyInfo = partyInfoService.getPartyInfo();
        final Set<Party> unseenParties =
            partyInfo.getParties().stream()
                .filter(p -> !p.getUrl().equals(partyInfo.getUrl()))
                .collect(Collectors.toSet());
        LOGGER.debug("Unseen parties {}", unseenParties);
        this.resendPartyStore.addUnseenParties(unseenParties);

        Optional<SyncableParty> nextPartyToSend = this.resendPartyStore.getNextParty();

        while (nextPartyToSend.isPresent()) {

            final SyncableParty requestDetails = nextPartyToSend.get();
            final String url = requestDetails.getParty().getUrl();

            final Runnable action =
                () -> {

                    // perform a sendPartyInfo in order to ensure that the target tessera has the current tessera as
                    // a recipient
                    boolean allSucceeded = updatePartyInfo(url);

                    if (allSucceeded) {
                        allSucceeded = this.transactionRequester.requestAllTransactionsFromNode(url);
                    }

                    if (!allSucceeded) {
                        this.resendPartyStore.incrementFailedAttempt(requestDetails);
                    }
                };

            this.executorService.submit(action);

            nextPartyToSend = this.resendPartyStore.getNextParty();
        }
    }

    private boolean updatePartyInfo(String url) {

        final WebSocketContainer container = ContainerProvider.getWebSocketContainer();

        final PartyInfoClientEndpoint endpoint = new PartyInfoClientEndpoint(partyInfoService);

        try {

            UriBuilder uriBuilder = UriBuilder.fromUri(URI.create(url)).path("sync");
            final URI uri = uriBuilder.build();

            final PartyInfo partyInfo = partyInfoService.getPartyInfo();

            SyncRequestMessage syncRequestMessage =
                SyncRequestMessage.Builder.create(SyncRequestMessage.Type.PARTY_INFO)
                    .withPartyInfo(partyInfo)
                    .build();

            Session session = container.connectToServer(endpoint, uri);
            LOGGER.debug("Connecting to server {}", uri);

            WebSocketSessionCallback.execute(
                () -> {
                    session.getBasicRemote().sendObject(syncRequestMessage);
                    return null;
                });

            return true;

        } catch (final Exception ex) {
            LOGGER.warn("Server error {} when connecting to {}", ex.getMessage(), url);
            LOGGER.debug(null, ex);
            return false;
        }
    }
}
