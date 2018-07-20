package com.github.tessera.node.model;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class PartyInfo {
    
    private final String url;

    private Set<Recipient> recipients;

    private Set<Party> parties;

    public PartyInfo(final String url, final Set<Recipient> recipients, final Set<Party> parties) {
        this.url = Objects.requireNonNull(url);
        this.recipients = Collections.unmodifiableSet(recipients);
        this.parties = Collections.unmodifiableSet(parties);
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
