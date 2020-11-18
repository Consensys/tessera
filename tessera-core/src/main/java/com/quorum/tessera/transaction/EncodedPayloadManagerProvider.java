package com.quorum.tessera.transaction;

import com.quorum.tessera.data.MessageHashFactory;
import com.quorum.tessera.enclave.Enclave;

public class EncodedPayloadManagerProvider {

    public static EncodedPayloadManager provider() {

        EncodedPayloadManagerHolder encodedPayloadManagerHolder = EncodedPayloadManagerHolder.INSTANCE;
        if(encodedPayloadManagerHolder.getEncodedPayloadManager().isPresent()) {
            return encodedPayloadManagerHolder.getEncodedPayloadManager().get();
        }

        Enclave enclave = Enclave.create();

        PrivacyHelper privacyHelper = PrivacyHelper.create();
        MessageHashFactory messageHashFactory = MessageHashFactory.create();

        EncodedPayloadManager encodedPayloadManager = new EncodedPayloadManagerImpl(enclave,privacyHelper,messageHashFactory);
        encodedPayloadManagerHolder.storeInstance(encodedPayloadManager);
        return encodedPayloadManager;
    }

}
