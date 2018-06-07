package com.github.nexus.service;

import com.github.nexus.enclave.keys.model.Key;
import com.github.nexus.entity.PartyInfo;

import javax.ws.rs.client.Client;

public class PartyInfoServiceImpl implements PartyInfoService {


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
         throw new UnsupportedOperationException("IMPLEMENT ME");
    }

    @Override
    public PartyInfo pollPartyInfo() {
         throw new UnsupportedOperationException("IMPLEMENT ME");
    }

    @Override
    public PartyInfo updatePartyInfo(byte[] encoded) {
        throw new UnsupportedOperationException("IMPLEMENT ME");
    }
}
