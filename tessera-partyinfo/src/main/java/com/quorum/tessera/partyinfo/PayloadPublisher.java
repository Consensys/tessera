package com.quorum.tessera.partyinfo;

import com.quorum.tessera.enclave.EncodedPayload;

/** Publishes messages from one node to another */
public interface PayloadPublisher {

    /**
     * Formats, encodes and publishes encrypted messages using the target public key as the identifier, instead of the
     * URL
     *
     * @param payload the pre-formatted payload object (i.e. with all recipients still present)
     * @param targetUrl the target public key to publish the payload to
     */
    void publishPayload(EncodedPayload payload, String targetUrl);
}
