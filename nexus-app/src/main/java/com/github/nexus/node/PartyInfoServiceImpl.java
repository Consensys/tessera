package com.github.nexus.node;


import com.github.nexus.configuration.Configuration;
import com.github.nexus.nacl.Key;
import com.github.nexus.node.model.Party;
import com.github.nexus.node.model.PartyInfo;
import com.github.nexus.node.model.Recipient;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;

public class PartyInfoServiceImpl implements PartyInfoService {

    private final PartyInfoStore partyInfoStore;

    public PartyInfoServiceImpl(final PartyInfoStore partyInfoStore, final Configuration configuration) {
        this.partyInfoStore = Objects.requireNonNull(partyInfoStore);

        final Set<Party> parties = configuration.othernodes()
            .stream()
            .map(Party::new)
            .collect(Collectors.toSet());

        partyInfoStore.store(new PartyInfo("", emptySet(), parties));
    }

    @Override
    public void registerPublicKeys(final String ourUrl, final Key[] publicKeys) {

        final Set<Recipient> ourKeys = Stream.of(publicKeys)
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
}
