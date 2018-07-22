package com.github.tessera.sync.model;

import com.github.tessera.node.model.Party;

public class SyncableParty {

    private final Party party;

    private final int attempts;

    public SyncableParty(final Party party, final int attempts) {
        this.party = party;
        this.attempts = attempts;
    }

    public Party getParty() {
        return party;
    }

    public int getAttempts() {
        return attempts;
    }

}
