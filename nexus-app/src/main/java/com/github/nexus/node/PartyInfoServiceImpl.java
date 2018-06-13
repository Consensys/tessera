package com.github.nexus.node;


import com.github.nexus.configuration.Configuration;
import com.github.nexus.enclave.keys.KeyManager;
import com.github.nexus.nacl.Key;
import com.github.nexus.node.model.Party;
import com.github.nexus.node.model.PartyInfo;
import com.github.nexus.node.model.Recipient;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;

public class PartyInfoServiceImpl implements PartyInfoService {

    private final PartyInfoStore partyInfoStore;


    public PartyInfoServiceImpl(final PartyInfoStore partyInfoStore,
                                final Configuration configuration,
                                final KeyManager keyManager) {

        this.partyInfoStore = Objects.requireNonNull(partyInfoStore);

        Objects.requireNonNull(keyManager);

        final String advertisedUrl = configuration.url() + ":" + Objects.toString(configuration.port());

        final Set<Party> parties = configuration.othernodes()
            .stream()
            .filter(url -> !url.isEmpty())
            .map(Party::new)
            .collect(Collectors.toSet());

        partyInfoStore.store(new PartyInfo(advertisedUrl, emptySet(), parties));

        registerPublicKeys(advertisedUrl, keyManager.getPublicKeys());

    }

    @Override
    public void registerPublicKeys(final String ourUrl, final Set<Key> publicKeys) {

        final Set<Recipient> ourKeys = publicKeys.stream()
            .map(key -> new Recipient(key, ourUrl))
            .collect(Collectors.toSet());

        final PartyInfo selfPartyInfo = new PartyInfo(ourUrl, ourKeys, emptySet());
        partyInfoStore.store(selfPartyInfo);
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
    public String getURLFromRecipientKey(Key key) {
        Recipient retrievedRecipientFromStore = partyInfoStore.getPartyInfo().getRecipients()
            .stream()
            .filter(recipient -> key.equals(recipient.getKey()))
            .findAny()
            .orElseThrow(() -> new RuntimeException("Recipient not found"));
        return retrievedRecipientFromStore.getUrl();
    }
}
