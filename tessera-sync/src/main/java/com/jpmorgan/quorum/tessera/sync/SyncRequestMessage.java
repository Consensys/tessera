package com.jpmorgan.quorum.tessera.sync;

import com.quorum.tessera.partyinfo.model.PartyInfo;
import java.util.Objects;

public class SyncRequestMessage {

    private final PartyInfo partyInfo;

    private SyncRequestMessage(PartyInfo partyInfo) {
        this.partyInfo = partyInfo;
    }

    public PartyInfo getPartyInfo() {
        return partyInfo;
    }

    public static class Builder {

        private PartyInfo partyInfo;

        private Builder() {}

        public Builder withPartyInfo(PartyInfo partyInfo) {
            this.partyInfo = partyInfo;
            return this;
        }

        public static Builder create() {
            return new Builder();
        }

        public SyncRequestMessage build() {
            Objects.requireNonNull(partyInfo);
            return new SyncRequestMessage(partyInfo);
        }
    }
}
