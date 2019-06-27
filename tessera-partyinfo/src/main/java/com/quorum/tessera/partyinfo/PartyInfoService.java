package com.quorum.tessera.partyinfo;

import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.encryption.PublicKey;

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

  /**
   * Retrieves the URL that the node is located at for the given public key
   *
   * @param key the public key to search for
   * @return the url the key's node is located at
   */
  String getURLFromRecipientKey(PublicKey key);

  PartyInfo removeRecipient(String uri);
}
