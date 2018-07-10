package com.github.tessera.node;

import com.github.tessera.config.ServerConfig;
import com.github.tessera.node.model.Party;
import com.github.tessera.node.model.PartyInfo;
import com.github.tessera.node.model.Recipient;

import java.util.Set;

import static java.util.Collections.emptySet;

public class PartyInfoStore {

    private PartyInfo partyInfo;

    public PartyInfoStore(final ServerConfig configuration) {

        final String advertisedUrl = configuration.getServerUri().toString();

        this.partyInfo = new PartyInfo(advertisedUrl, emptySet(), emptySet());
    }

    public synchronized void store(final PartyInfo partyInfoToUpdate) {
        final Set<Recipient> existingRecipients = this.partyInfo.getRecipients();
        final Set<Recipient> newRecipients = partyInfoToUpdate.getRecipients();

        existingRecipients.addAll(newRecipients);

        final Set<Party> existingParties = this.partyInfo.getParties();
        final Set<Party> newParties = partyInfoToUpdate.getParties();
        existingParties.addAll(newParties);
    }

    public synchronized PartyInfo getPartyInfo() {
        return new PartyInfo(partyInfo);
    }

}
