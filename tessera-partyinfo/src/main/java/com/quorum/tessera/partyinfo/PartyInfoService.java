package com.quorum.tessera.partyinfo;

import com.quorum.tessera.enclave.EncodedPayload;
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

    // Set<String> getUrlsForKey(PublicKey key);

    PartyInfo removeRecipient(String uri);

    /**
     * Formats, encodes and publishes encrypted messages using the target public key as the identifier, instead of the
     * URL
     *
     * @param payload the pre-formatted payload object (i.e. with all recipients still present)
     * @param recipientKey the target public key to publish the payload to
     * @throws KeyNotFoundException if the target public key is not known
     */
    void publishPayload(EncodedPayload payload, PublicKey recipientKey);
}
