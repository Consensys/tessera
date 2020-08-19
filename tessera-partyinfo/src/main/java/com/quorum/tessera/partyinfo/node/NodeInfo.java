package com.quorum.tessera.partyinfo.node;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public interface NodeInfo {

    Set<Party> getParties();

    Set<Recipient> getRecipients();

    Set<String> supportedApiVersions();

    String getUrl();

    class Builder {

        private String url;

        private final Set<Party> parties = new HashSet<>();

        private final Set<Recipient> recipients = new HashSet<>();

        private Set<String> supportedApiVersions = new HashSet<>();

        public Builder withParties(Collection<Party> parties) {
            this.parties.addAll(parties);
            return this;
        }

        public Builder withRecipients(Collection<Recipient> recipients) {
            this.recipients.addAll(recipients);
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
                    return Set.copyOf(parties);
                }

                @Override
                public Set<Recipient> getRecipients() {
                    return Set.copyOf(recipients);
                }

                @Override
                public Set<String> supportedApiVersions() {
                    return Set.copyOf(supportedApiVersions);
                }

                @Override
                public String getUrl() {
                    return url;
                }

                @Override
                public String toString() {
                    return String.format("NodeInfo[url: %s ,parties: %s, recipients: %s]",url, parties,recipients);
                }
            };
        }

        public static Builder create() {
            return new Builder() {};
        }

        public static Builder from(NodeInfo nodeInfo) {
            return create()
                .withRecipients(nodeInfo.getRecipients())
                .withUrl(nodeInfo.getUrl())
                .withParties(nodeInfo.getParties())
                .withSupportedApiVersions(nodeInfo.supportedApiVersions());
        }

        private Builder() {}

        public Builder withUrl(String url) {
            this.url = url;
            return this;
        }
    }


}
