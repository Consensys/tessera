package com.quorum.tessera.node;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.Peer;
import com.quorum.tessera.key.KeyManager;
import com.quorum.tessera.key.exception.KeyNotFoundException;
import com.quorum.tessera.nacl.Key;
import com.quorum.tessera.node.model.Party;
import com.quorum.tessera.node.model.PartyInfo;
import com.quorum.tessera.node.model.Recipient;

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
    public Set<Party> findUnsavedParties(final PartyInfo partyInfoWithUnsavedRecipients) {
        final Set<Party> knownHosts = this.getPartyInfo().getParties();

        final Set<Party> incomingRecipients = new HashSet<>(partyInfoWithUnsavedRecipients.getParties());
        incomingRecipients.removeAll(knownHosts);

        return Collections.unmodifiableSet(incomingRecipients);
    }

}
