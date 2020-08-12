package com.quorum.tessera.transaction.publish;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.PublicKey;

/** Publishes messages from one node to another */
public interface PayloadPublisher {

    /**
     * Formats, encodes and publishes encrypted messages using the target public key as the identifier, instead of the
     * URL
     *
     * @param payload the pre-formatted payload object (i.e. with all recipients still present)
     * @param recipienyKey the target public key to publish the payload to
     */
    void publishPayload(EncodedPayload payload, PublicKey recipienyKey);
}
