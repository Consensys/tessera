package com.github.nexus.service;

import com.github.nexus.enclave.keys.model.Key;
import com.github.nexus.entity.PartyInfo;

import javax.ws.rs.client.Client;

public interface PartyInfoService {


    void initPartyInfo(String rawUrl, String[] otherNodes, Client client);

    void registerPublicKeys(Key[] publicKeys);

    /**
     * Request PartyInfo data from all remote nodes that this node is aware of.
     * @return PartyInfo object
     */
    PartyInfo getPartyInfo();

    /**
     * Poll to request PartyInfo data.
     * Time invervals should be configurable.
     * @return PartyInfo object
     */
    PartyInfo pollPartyInfo();

    /**
     * Update the PartyInfo data store with the provided encoded data.
     * This can happen when endpoint /partyinfo is triggered,
     * or by a response from this node hitting another node /partyinfo endpoint
     * @param encoded encoded payload
     * @return updated PartyInfo object
     */
    PartyInfo updatePartyInfo(byte[] encoded);



}
