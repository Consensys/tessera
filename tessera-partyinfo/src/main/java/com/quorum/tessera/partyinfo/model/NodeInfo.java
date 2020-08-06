package com.quorum.tessera.partyinfo.model;

public interface NodeInfo {

    PartyInfo partyInfo();

    VersionInfo versionInfo();

    class Builder {

        private PartyInfo partyInfo;

        private VersionInfo versionInfo;

        public Builder from(PartyInfo partyInfo) {
            this.partyInfo = partyInfo;
            return this;
        }

        public Builder withVersionInfo(VersionInfo versionInfo) {
            this.versionInfo = versionInfo;
            return this;
        }

        public NodeInfo build() {
            return new NodeInfo() {
                @Override
                public PartyInfo partyInfo() {
                    return partyInfo;
                }

                @Override
                public VersionInfo versionInfo() {
                    return versionInfo;
                }
            };
        }

        public static Builder create() {
            return new Builder() {};
        }

    }

}
