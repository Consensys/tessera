package com.quorum.tessera.partyinfo;

import com.quorum.tessera.partyinfo.model.PartyInfo;

public interface PartyInfoService {

    /**
     * Request PartyInfo data from all remote nodes that this node is aware of
     *
     * @return PartyInfo object
     */
    PartyInfo getPartyInfo();

    /**
     * Update the PartyInfo data store with the provided encoded data.This can happen when endpoint /partyinfo is
     * triggered, or by a response from this node hitting another node /partyinfo endpoint
     *
     * @param partyInfo
     * @return updated PartyInfo object
     */
    PartyInfo updatePartyInfo(PartyInfo partyInfo);

    // Set<String> getUrlsForKey(PublicKey key);

    PartyInfo removeRecipient(String uri);
}
