package com.github.tessera.sync;

import com.github.tessera.config.Config;
import com.github.tessera.config.Peer;
import com.github.tessera.node.model.Party;
import com.github.tessera.sync.model.SyncableParty;

import java.util.*;

import static java.util.stream.Collectors.toSet;

public class ResendPartyStoreImpl implements ResendPartyStore {

    private Queue<SyncableParty> outstandingParties;

    public ResendPartyStoreImpl(final Config config) {
        this.outstandingParties = new LinkedList<>();

        final Set<Party> initialParties = config
            .getPeers()
            .stream()
            .map(Peer::getUrl)
            .map(Party::new)
            .collect(toSet());

        this.addUnseenParties(initialParties);
    }

    @Override
    public void addUnseenParties(final Collection<Party> partiesToRequestFrom) {
        partiesToRequestFrom
            .stream()
            .map(party -> new SyncableParty(party, 0))
            .forEach(outstandingParties::add);
    }

    @Override
    public Optional<SyncableParty> getNextParty() {
        return Optional.ofNullable(this.outstandingParties.poll());
    }

    @Override
    public void incrementFailedAttempt(final SyncableParty attemptedParty) {

        if (attemptedParty.getAttempts() < MAX_ATTEMPTS) {
            final SyncableParty updatedParty = new SyncableParty(
                attemptedParty.getParty(), attemptedParty.getAttempts() + 1
            );

            this.outstandingParties.add(updatedParty);
        }

    }

}
