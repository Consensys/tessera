package com.quorum.tessera.node;

import com.quorum.tessera.nacl.Key;
import com.quorum.tessera.node.model.Party;
import com.quorum.tessera.node.model.PartyInfo;

import java.util.Set;

public interface PartyInfoService {

    /**
     * Request PartyInfo data from all remote nodes that this node is aware of
     *
     * @return PartyInfo object
     */
    PartyInfo getPartyInfo();

    /**
     * Update the PartyInfo data store with the provided encoded data.
     * This can happen when endpoint /partyinfo is triggered,
     * or by a response from this node hitting another node /partyinfo endpoint
     *
     * @return updated PartyInfo object
     */
    PartyInfo updatePartyInfo(PartyInfo partyInfo);

    /**
     * Retrieves the URL that the node is located at for the given public key
     *
     * @param key the public key to search for
     * @return the url the key's node is located at
     */
    String getURLFromRecipientKey(Key key);

    /**
     * Searches the provided {@link PartyInfo} for recipients that haven't yet been saved to
     * the list of all known hosts
     *
     * @param partyInfoWithUnsavedRecipients received party info that should be diffed
     * @return the list of all unknown recipients
     */
    Set<Party> findUnsavedParties(PartyInfo partyInfoWithUnsavedRecipients);

}
