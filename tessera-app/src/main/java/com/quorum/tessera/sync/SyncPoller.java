package com.quorum.tessera.sync;

import com.quorum.tessera.sync.model.SyncableParty;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

/**
 * A poller that will contact all outstanding parties that need to have
 * transactions resent for a single round
 */
public class SyncPoller implements Runnable {

    private final ExecutorService executorService;

    private final ResendPartyStore resendPartyStore;

    private final TransactionRequester transactionRequester;

    public SyncPoller(final ExecutorService executorService,
                      final ResendPartyStore resendPartyStore,
                      final TransactionRequester transactionRequester) {
        this.executorService = Objects.requireNonNull(executorService);
        this.resendPartyStore = Objects.requireNonNull(resendPartyStore);
        this.transactionRequester = Objects.requireNonNull(transactionRequester);
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
                final boolean allSucceeded = this.transactionRequester.requestAllTransactionsFromNode(url);

                if (!allSucceeded) {
                    this.resendPartyStore.incrementFailedAttempt(requestDetails);
                }
            };

            this.executorService.submit(action);

            nextPartyToSend = this.resendPartyStore.getNextParty();

        }

    }

}
