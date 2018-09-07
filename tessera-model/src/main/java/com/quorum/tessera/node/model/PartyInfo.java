package com.quorum.tessera.node.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Contains all information that is transferred between two nodes on the
 * network, including:
 * - the external URL of this node
 * - all known {@link Recipient} that contains public key to URL mappings
 * - all known URLs on the network
 */
public class PartyInfo {
    
    private final String url;

    private Set<Recipient> recipients;

    private Set<Party> parties;

    public PartyInfo(final String url, final Set<Recipient> recipients, final Set<Party> parties) {
        this.url = Objects.requireNonNull(url);
        this.recipients = Collections.unmodifiableSet(new HashSet<>(recipients));
        this.parties = Collections.unmodifiableSet(new HashSet<>(parties));
    }

    public String getUrl() {
        return url;
    }

    public Set<Recipient> getRecipients() {
        return recipients;
    }

    public Set<Party> getParties() {
        return parties;
    }

}
