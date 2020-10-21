package com.quorum.tessera.recovery.resend;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.KeyNotFoundException;


import java.util.List;

/** Publishes messages from one node to another */
public interface ResendBatchPublisher {

    /**
     * Formats, encodes and publishes encrypted messages using the target public key as the identifier, instead of the
     * URL
     *
     * @param payload
     * @param targetUrl
     * @throws KeyNotFoundException if the target public key is not known
     */
    void publishBatch(List<EncodedPayload> payload, String targetUrl);


}
