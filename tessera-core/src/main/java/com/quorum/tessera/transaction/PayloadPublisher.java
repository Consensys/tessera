package com.quorum.tessera.transaction;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.KeyNotFoundException;
import com.quorum.tessera.encryption.PublicKey;

/** Publishes messages from one node to another */
public interface PayloadPublisher {

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
