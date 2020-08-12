package com.quorum.tessera.p2p;

import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.TransactionRequester;
import com.quorum.tessera.partyinfo.model.Party;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * A poller that will contact all outstanding parties that need to have transactions resent for a single round
 */
public class SyncPoller implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SyncPoller.class);

    private final ExecutorService executorService;

    private final ResendPartyStore resendPartyStore;

    private final TransactionRequester transactionRequester;

    private final PartyInfoService partyInfoService;

    private final NodeInfoPublisher nodeInfoPublisher;

    public SyncPoller(final ResendPartyStore resendPartyStore,
                      final TransactionRequester transactionRequester,
                      final PartyInfoService partyInfoService,
                      final NodeInfoPublisher publisher) {
        this(
            Executors.newCachedThreadPool(),
            resendPartyStore,
            transactionRequester,
            partyInfoService,
            publisher
        );
    }

    public SyncPoller(final ExecutorService executorService,
                      final ResendPartyStore resendPartyStore,
                      final TransactionRequester transactionRequester,
                      final PartyInfoService partyInfoService,
                      final NodeInfoPublisher nodeInfoPublisher) {
        this.executorService = Objects.requireNonNull(executorService);
        this.resendPartyStore = Objects.requireNonNull(resendPartyStore);
        this.transactionRequester = Objects.requireNonNull(transactionRequester);
        this.partyInfoService = Objects.requireNonNull(partyInfoService);
        this.nodeInfoPublisher = Objects.requireNonNull(nodeInfoPublisher);
    }

    /**
     * Retrieves all of the outstanding parties and makes an attempt to make the resend request asynchronously. If the
     * request fails then the party is submitted back to the store for a later attempt.
     */
    @Override
    public void run() {

        final PartyInfo partyInfo = PartyInfo.from(partyInfoService.getPartyInfo());

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
        try {
            final NodeInfo nodeInfo = partyInfoService.getPartyInfo();

            // we deliberately discard the response as we do not want to fully duplicate the PartyInfoPoller
            return nodeInfoPublisher.publishNodeInfo(url, nodeInfo);
        } catch (final Exception ex) {
            LOGGER.warn("Failed to connect to node {} for partyinfo, due to {}", url, ex.getMessage());
            LOGGER.debug(null, ex);
            return false;
        }
    }
}
