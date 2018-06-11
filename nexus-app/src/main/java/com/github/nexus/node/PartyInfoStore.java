package com.github.nexus.node;

import com.github.nexus.configuration.Configuration;
import com.github.nexus.node.model.Party;
import com.github.nexus.node.model.PartyInfo;
import com.github.nexus.node.model.Recipient;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class PartyInfoStore {

    private PartyInfo partyInfo;

    public PartyInfoStore(final Configuration configuration) {

        final String advertisedUrl = configuration.url() + ":" + Objects.toString(configuration.port());

        this.partyInfo = new PartyInfo(advertisedUrl, Collections.emptySet(), Collections.emptySet());
    }

    public void store(final PartyInfo partyInfo) {
        final Set<Recipient> existingRecipients = this.partyInfo.getRecipients();
        final Set<Recipient> newRecipients = partyInfo.getRecipients();
        existingRecipients.addAll(newRecipients);

        final Set<Party> existingParties = this.partyInfo.getParties();
        final Set<Party> newParties = partyInfo.getParties();
        existingParties.addAll(newParties);
    }

    public PartyInfo getPartyInfo() {
        return partyInfo;
    }

}
