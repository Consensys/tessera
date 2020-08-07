package com.quorum.tessera.partyinfo.model;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

public interface NodeInfo {

    Set<Party> getParties();

    Set<Recipient> getRecipients();

    Set<String> supportedApiVersions();

    String getUrl();

    class Builder {

        private String url;

        private Set<Party> parties = Set.of();

        private Set<Recipient> recipients = Set.of();

        private Set<String> supportedApiVersions = Set.of();

        public Builder withParties(Collection<Party> parties) {
            this.parties = Set.copyOf(parties);
            return this;
        }

        public Builder withRecipients(Collection<Recipient> recipients) {
            this.recipients = Set.copyOf(recipients);
            return this;
        }

        public Builder withSupportedApiVersions(Collection<String> supportedApiVersions) {
            if(Objects.nonNull(supportedApiVersions)) {
                this.supportedApiVersions = Set.copyOf(supportedApiVersions);
            }
            return this;
        }

        public NodeInfo build() {

            Objects.requireNonNull(url,"URL is required");

            return new NodeInfo() {

                @Override
                public Set<Party> getParties() {
                    return parties;
                }

                @Override
                public Set<Recipient> getRecipients() {
                    return recipients;
                }

                @Override
                public Set<String> supportedApiVersions() {
                    return supportedApiVersions;
                }

                @Override
                public String getUrl() {
                    return url;
                }
            };
        }

        public static Builder create() {
            return new Builder() {};
        }

        private Builder() {}

        public Builder withUrl(String url) {
            this.url = url;
            return this;
        }
    }


    static NodeInfo from(PartyInfo partyInfo, Collection<String> versions) {
        return NodeInfo.Builder.create()
            .withUrl(partyInfo.getUrl())
            .withRecipients(partyInfo.getRecipients())
            .withParties(partyInfo.getParties())
            .withSupportedApiVersions(versions)
            .build();

    }
}
