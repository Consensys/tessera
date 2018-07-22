package com.github.tessera.sync;

import com.github.tessera.node.model.Party;
import com.github.tessera.sync.model.SyncableParty;

import java.util.Collection;
import java.util.Optional;

public interface ResendPartyStore {

    int MAX_ATTEMPTS = 20;

    void addUnseenParties(Collection<Party> partiesToRequestFrom);

    Optional<SyncableParty> getNextParty();

    void incrementFailedAttempt(SyncableParty attemptedParty);

}
