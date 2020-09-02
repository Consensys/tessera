package com.quorum.tessera.transaction;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.data.EntityManagerDAOFactory;
import com.quorum.tessera.data.MessageHashFactory;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.PublicKey;

public interface EncodedPayloadManager {

    EncodedPayload create(SendRequest request);

    ReceiveResponse decrypt(EncodedPayload payload, PublicKey maybeDefaultRecipient);

    static EncodedPayloadManager create(Config config) {
        return ServiceLoaderUtil.load(EncodedPayloadManager.class).orElseGet(() -> {
            final Enclave enclave = EnclaveFactory.create().create(config);
            final EntityManagerDAOFactory emDAOFactory = EntityManagerDAOFactory.newFactory(config);
            final PrivacyHelper privacyHelper = new PrivacyHelperImpl(emDAOFactory.createEncryptedTransactionDAO());

            return new EncodedPayloadManagerImpl(enclave, privacyHelper, MessageHashFactory.create());
        });
    }
}
