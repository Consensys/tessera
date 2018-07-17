package com.github.tessera.node;

import com.github.tessera.config.Config;
import com.github.tessera.config.Peer;
import com.github.tessera.key.KeyManager;
import com.github.tessera.key.exception.KeyNotFoundException;
import com.github.tessera.nacl.Key;
import com.github.tessera.node.model.Party;
import com.github.tessera.node.model.PartyInfo;
import com.github.tessera.node.model.Recipient;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class PartyInfoServiceImpl implements PartyInfoService {

    private final PartyInfoStore partyInfoStore;

    public PartyInfoServiceImpl(final PartyInfoStore partyInfoStore,
                                final Config configuration,
                                final KeyManager keyManager) {
        this.partyInfoStore = Objects.requireNonNull(partyInfoStore);

        final String advertisedUrl = configuration.getServerConfig().getServerUri().toString();

        final Set<Party> initialParties = configuration
            .getPeers()
            .stream()
            .map(Peer::getUrl)
            .map(Party::new)
            .collect(toSet());

        final Set<Recipient> ourKeys = keyManager
            .getPublicKeys()
            .stream()
            .map(key -> new Recipient(key, advertisedUrl))
            .collect(toSet());

        partyInfoStore.store(new PartyInfo(advertisedUrl, ourKeys, initialParties));
    }

    @Override
    public PartyInfo getPartyInfo() {
        return partyInfoStore.getPartyInfo();
    }

    @Override
    public PartyInfo updatePartyInfo(final PartyInfo partyInfo) {

        partyInfoStore.store(partyInfo);

        return this.getPartyInfo();
    }

    @Override
    public String getURLFromRecipientKey(final Key key) {

        final Recipient retrievedRecipientFromStore = partyInfoStore
            .getPartyInfo()
            .getRecipients()
            .stream()
            .filter(recipient -> key.equals(recipient.getKey()))
            .findAny()
            .orElseThrow(() -> new KeyNotFoundException("Recipient not found"));

        return retrievedRecipientFromStore.getUrl();
    }

    @Override
    public Set<Recipient> findUnsavedRecipients(final PartyInfo partyInfoWithUnsavedRecipients) {
        final Set<Recipient> knownHosts = this.getPartyInfo().getRecipients();

        final Set<Recipient> incomingRecipients = new HashSet<>(partyInfoWithUnsavedRecipients.getRecipients());
        incomingRecipients.removeAll(knownHosts);

        return Collections.unmodifiableSet(incomingRecipients);
    }

}
