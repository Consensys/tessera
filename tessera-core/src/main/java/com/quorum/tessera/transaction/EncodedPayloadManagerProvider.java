package com.quorum.tessera.transaction;

import com.quorum.tessera.data.MessageHashFactory;
import com.quorum.tessera.enclave.Enclave;

public class EncodedPayloadManagerProvider {

    public static EncodedPayloadManager provider() {

        Enclave enclave = Enclave.create();

        PrivacyHelper privacyHelper = PrivacyHelper.create();
        MessageHashFactory messageHashFactory = MessageHashFactory.create();

        return new EncodedPayloadManagerImpl(enclave,privacyHelper,messageHashFactory);
    }

}
