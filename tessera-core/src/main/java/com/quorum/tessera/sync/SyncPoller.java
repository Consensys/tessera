package com.quorum.tessera.sync;

import com.quorum.tessera.client.P2pClient;
import com.quorum.tessera.node.PartyInfoParser;
import com.quorum.tessera.node.PartyInfoService;
import com.quorum.tessera.node.model.PartyInfo;
import com.quorum.tessera.sync.model.SyncableParty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

/**
 * A poller that will contact all outstanding parties that need to have
 * transactions resent for a single round
 */
public class SyncPoller implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SyncPoller.class);

    private final ExecutorService executorService;

    private final ResendPartyStore resendPartyStore;

    private final TransactionRequester transactionRequester;

    private final PartyInfoService partyInfoService;

    private final P2pClient p2pClient;

    private final PartyInfoParser partyInfoParser;

    public SyncPoller(final ExecutorService executorService,
                      final ResendPartyStore resendPartyStore,
                      final TransactionRequester transactionRequester,
                      final PartyInfoService partyInfoService,
                      final PartyInfoParser partyInfoParser,
                      final P2pClient p2pClient) {
        this.executorService = Objects.requireNonNull(executorService);
        this.resendPartyStore = Objects.requireNonNull(resendPartyStore);
        this.transactionRequester = Objects.requireNonNull(transactionRequester);
        this.partyInfoService = Objects.requireNonNull(partyInfoService);
        this.partyInfoParser = Objects.requireNonNull(partyInfoParser);
        this.p2pClient = Objects.requireNonNull(p2pClient);
    }

    /**
     * Retrieves all of the outstanding parties and makes an attempt to make the resend request
     * asynchronously. If the request fails then the party is submitted back to the store for
     * a later attempt.
     */
    @Override
    public void run() {

        Optional<SyncableParty> nextPartyToSend = this.resendPartyStore.getNextParty();

        while (nextPartyToSend.isPresent()) {

            final SyncableParty requestDetails = nextPartyToSend.get();
            final String url = requestDetails.getParty().getUrl();

            final Runnable action = () -> {

                // perform a getPartyInfo in order to ensure that the target tessera has the current tessera as a recipient
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
            final PartyInfo partyInfo = partyInfoService.getPartyInfo();

            final byte[] encodedPartyInfo = partyInfoParser.to(partyInfo);

            // we deliberately discard the response as we do not want to fully duplicate the PartyInfoPoller
            return null != p2pClient.getPartyInfo(url, encodedPartyInfo);
        } catch (final Exception ex) {
            LOGGER.warn("Server error {} when connecting to {}", ex.getMessage(), url);
            LOGGER.debug(null, ex);
            return false;
        }
    }

}
