package com.github.nexus.node;

import com.github.nexus.enclave.keys.model.Key;

public interface PartyInfoService {

    /**
     *
     * @param rawUrl
     * @param otherNodes
     */
    void initPartyInfo(String rawUrl, String[] otherNodes);


    /**
     *
     * @param publicKeys
     */
    void registerPublicKeys(Key[] publicKeys);

    /**
     * Request PartyInfo data from all remote nodes that this node is aware of.
     * @return PartyInfo object
     */
    PartyInfo getPartyInfo();

    /**
     * Update the PartyInfo data store with the provided encoded data.
     * This can happen when endpoint /partyinfo is triggered,
     * or by a response from this node hitting another node /partyinfo endpoint
     * @return updated PartyInfo object
     */
    PartyInfo updatePartyInfo(PartyInfo partyInfo);



}
