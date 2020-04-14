package com.quorum.tessera.partyinfo;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.KeyNotFoundException;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.model.Recipient;

import java.util.List;

/** Publishes messages from one node to another */
public interface ResendBatchPublisher {

    void publishBatch(List<EncodedPayload> payload, String targetUrl);

    /**
     * Formats, encodes and publishes encrypted messages using the target public key as the identifier, instead of the
     * URL
     *
     * @param payload the pre-formatted payload object (i.e. with all recipients still present)
     * @param recipientKey the target public key to publish the payload to
     * @throws KeyNotFoundException if the target public key is not known
     */
    default void publishBatch(List<EncodedPayload> payload, PublicKey recipientKey) {
        //        Recipient recipient = PartyInfoStore.get().findRecipientByPublicKey(recipientKey);
        //        this.publishBatch(payload, recipient.getUrl());
    }
}
