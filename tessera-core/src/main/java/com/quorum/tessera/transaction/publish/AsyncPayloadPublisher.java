package com.quorum.tessera.transaction.publish;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.PublicKey;

import java.util.List;

public interface AsyncPayloadPublisher {

    /**
     * Asynchronously strips (leaving data intended only for that particular recipient) and publishes the payload to
     * each recipient identified by the provided keys.
     * This method blocks until all pushes return successfully; if a push fails with an exception, the method exits
     * immediately and does not wait for the remaining responses.
     *
     * @param payload the payload object to be stripped and pushed
     * @param recipientKeys list of public keys identifying the target nodes
     */
    void publishPayload(EncodedPayload payload, List<PublicKey> recipientKeys);

}
