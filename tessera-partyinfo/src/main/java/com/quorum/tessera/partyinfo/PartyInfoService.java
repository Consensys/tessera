package com.quorum.tessera.partyinfo;

import com.quorum.tessera.partyinfo.node.NodeInfo;

public interface PartyInfoService {

    /**
     * Request PartyInfo data from all remote nodes that this node is aware of
     *
     * @return NodeInfo object
     */
    NodeInfo getPartyInfo();

    /**
     * Update the PartyInfo data store with the provided encoded data.This can happen when endpoint /partyinfo is
     * triggered, or by a response from this node hitting another node /partyinfo endpoint
     *
     * @param partyInfo
     * @return updated PartyInfo object
     */
    NodeInfo updatePartyInfo(NodeInfo partyInfo);

    NodeInfo removeRecipient(String uri);

    // TODO: Added as lifecycle call once RuntimeContext has been created.
    void populateStore();

    void syncKeys();
}
