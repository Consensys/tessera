package com.github.nexus.service;

import com.github.nexus.enclave.keys.model.Key;
import com.github.nexus.entity.PartyInfo;

import javax.ws.rs.client.Client;
import java.util.logging.Logger;

public class PartyInfoServiceImpl implements PartyInfoService {

    private static final Logger LOGGER = Logger.getLogger(PartyInfoServiceImpl.class.getName());

    private static final String ENDPOINT_URL = "/partyinfo";

    private static PartyInfo partyInfo;

    @Override
    public void initPartyInfo(String rawUrl, String[] otherNodes, Client client) {

    }

    @Override
    public void registerPublicKeys(Key[] publicKeys) {
    }

    @Override
    public PartyInfo getPartyInfo() {
        return null;
    }

    @Override
    public PartyInfo pollPartyInfo() {
        return null;
    }

    @Override
    public PartyInfo updatePartyInfo(byte[] encoded) {
        return null;
    }
}
