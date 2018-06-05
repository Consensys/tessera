package com.github.nexus.service;

import com.github.nexus.entity.PartyInfo;

public interface PartyInfoService {

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
