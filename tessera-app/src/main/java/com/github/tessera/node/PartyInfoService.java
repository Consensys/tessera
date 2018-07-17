package com.github.tessera.node;

import com.github.tessera.nacl.Key;
import com.github.tessera.node.model.PartyInfo;

public interface PartyInfoService {

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

    String getURLFromRecipientKey(Key key);

}
