package com.github.tessera.sync;

import com.github.tessera.sync.model.SyncableParty;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

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
