package com.github.nexus.node;

import com.github.nexus.enclave.keys.model.Key;

import javax.ws.rs.client.Client;

public class PartyInfoServiceImpl implements PartyInfoService {

    private PartyInfoStore partyInfoStore = PartyInfoStore.INSTANCE;

    @Override
    public void initPartyInfo(String rawUrl, String[] otherNodes, Client client) {
           throw new UnsupportedOperationException("IMPLEMENT ME");
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
