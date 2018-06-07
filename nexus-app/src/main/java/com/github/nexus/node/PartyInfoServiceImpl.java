package com.github.nexus.node;

import com.github.nexus.enclave.keys.model.Key;

import java.util.stream.Stream;

public class PartyInfoServiceImpl implements PartyInfoService {

    private PartyInfoStore partyInfoStore = PartyInfoStore.INSTANCE;

    @Override
    public void initPartyInfo(String url, String[] otherNodes) {
        Party[] parties = Stream.of(otherNodes)
            .map(node -> new Party(node)).toArray(Party[]::new);

        partyInfoStore.store(new PartyInfo(url, new Recipient[]{}, parties));
    }

    @Override
    public void registerPublicKeys(Key[] publicKeys) {
         throw new UnsupportedOperationException("IMPLEMENT ME");
    }

    @Override
    public PartyInfo getPartyInfo() {
        return partyInfoStore.getPartyInfo();
    }

    @Override
    public PartyInfo updatePartyInfo(PartyInfo partyInfo) {

        partyInfoStore.store(partyInfo);

        return partyInfoStore.getPartyInfo();
    }
}
