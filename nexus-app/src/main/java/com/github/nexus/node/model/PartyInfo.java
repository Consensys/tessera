package com.github.nexus.node.model;

import java.util.*;

public class PartyInfo {
    
    private final String url;

    private Set<Recipient> recipients;

    private Set<Party> parties;

    public PartyInfo(final String url, final Set<Recipient> recipients, final Set<Party> parties) {
        this.url = Objects.requireNonNull(url);
        this.recipients = Collections.synchronizedSet(new HashSet<>(recipients));
        this.parties = Collections.synchronizedSet(new HashSet<>(parties));
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
