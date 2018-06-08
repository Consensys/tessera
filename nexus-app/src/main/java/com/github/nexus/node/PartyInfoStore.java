package com.github.nexus.node;

public enum PartyInfoStore {

    INSTANCE;

    private PartyInfo partyInfo;

    public void store(PartyInfo partyInfo) {
        this.partyInfo  = partyInfo;
    }

    public PartyInfo getPartyInfo() {
        return partyInfo;
    }
}
