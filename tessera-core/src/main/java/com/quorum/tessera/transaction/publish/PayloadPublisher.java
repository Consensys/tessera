package com.quorum.tessera.transaction.publish;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.PublicKey;

import java.util.ServiceLoader;

/** Publishes messages from one node to another */
public interface PayloadPublisher {

    /**
     * Publishes an EncodedPayload to the recipient identified by the provided key
     *
     * @param payload the payload object to be pushed
     * @param recipientKey the public key identifying the target node
     */
    void publishPayload(EncodedPayload payload, PublicKey recipientKey);

    CommunicationType communicationType();

    static PayloadPublisher create() {
        Config config = ConfigFactory.create().getConfig();
        CommunicationType communicationType = config.getP2PServerConfig().getCommunicationType();
        return ServiceLoader.load(PayloadPublisher.class).stream()
            .map(ServiceLoader.Provider::get)
            .filter(p -> p.communicationType() == communicationType)
            .findFirst().get();

    }

}
