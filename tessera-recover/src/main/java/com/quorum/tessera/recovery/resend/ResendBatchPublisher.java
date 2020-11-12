package com.quorum.tessera.recovery.resend;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.KeyNotFoundException;

import java.util.List;
import java.util.ServiceLoader;

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

    CommunicationType communicationType();

    static ResendBatchPublisher create() {

        Config config = ConfigFactory.create().getConfig();
        CommunicationType communicationType = config.getP2PServerConfig().getCommunicationType();
        return ServiceLoader.load(ResendBatchPublisher.class)
            .stream()
            .map(ServiceLoader.Provider::get)
            .filter(p -> p.communicationType() == communicationType)
            .findFirst()
            .orElseThrow(() ->
            new UnsupportedOperationException(
                "Unable to create a ResendBatchPublisherFactory for "
                    + communicationType));

    }

}
