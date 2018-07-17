package com.github.tessera.node;

import com.github.tessera.config.Config;
import com.github.tessera.config.Peer;
import com.github.tessera.key.KeyManager;
import com.github.tessera.key.exception.KeyNotFoundException;
import com.github.tessera.nacl.Key;
import com.github.tessera.node.model.Party;
import com.github.tessera.node.model.PartyInfo;
import com.github.tessera.node.model.Recipient;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class PartyInfoServiceImpl implements PartyInfoService {

    private final PartyInfoStore partyInfoStore;

    public PartyInfoServiceImpl(final PartyInfoStore partyInfoStore,
                                final Config configuration,
                                final KeyManager keyManager) {

        this.partyInfoStore = Objects.requireNonNull(partyInfoStore);

        final String advertisedUrl = configuration.getServerConfig().getServerUri().toString();

        final Set<Party> initialParties = configuration.getPeers()
            .stream()
            .map(Peer::getUrl)
            .map(Party::new)
            .collect(Collectors.toSet());

        final Set<Recipient> ourKeys = keyManager
            .getPublicKeys()
            .stream()
            .map(key -> new Recipient(key, advertisedUrl))
            .collect(Collectors.toSet());

        partyInfoStore.store(new PartyInfo(advertisedUrl, ourKeys, initialParties));
    }

    @Override
    public PartyInfo getPartyInfo() {
        return partyInfoStore.getPartyInfo();
    }

    @Override
    public PartyInfo updatePartyInfo(final PartyInfo partyInfo) {

        partyInfoStore.store(partyInfo);

        return partyInfoStore.getPartyInfo();
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

}
