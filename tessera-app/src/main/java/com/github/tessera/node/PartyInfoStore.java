package com.github.tessera.node;

import com.github.tessera.config.ServerConfig;
import com.github.tessera.node.model.Party;
import com.github.tessera.node.model.PartyInfo;
import com.github.tessera.node.model.Recipient;

import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;

public class PartyInfoStore {

    private final String advertisedUrl;

    private final Set<Recipient> recipients;

    private final Set<Party> parties;

    public PartyInfoStore(final ServerConfig configuration) {

        this.advertisedUrl = configuration.getServerUri().toString();

        this.recipients = new HashSet<>();
        this.parties = new HashSet<>();
    }

    public synchronized void store(final PartyInfo newInfo) {
        recipients.addAll(newInfo.getRecipients());
        parties.addAll(newInfo.getParties());
    }

    public synchronized PartyInfo getPartyInfo() {
        return new PartyInfo(advertisedUrl, unmodifiableSet(recipients), unmodifiableSet(parties));
    }

}
