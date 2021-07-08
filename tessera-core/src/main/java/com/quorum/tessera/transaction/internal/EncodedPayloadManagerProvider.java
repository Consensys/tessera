package com.quorum.tessera.transaction.internal;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.PayloadDigest;
import com.quorum.tessera.transaction.EncodedPayloadManager;
import com.quorum.tessera.transaction.PrivacyHelper;

public class EncodedPayloadManagerProvider {

  public static EncodedPayloadManager provider() {

    EncodedPayloadManagerHolder encodedPayloadManagerHolder = EncodedPayloadManagerHolder.INSTANCE;
    if (encodedPayloadManagerHolder.getEncodedPayloadManager().isPresent()) {
      return encodedPayloadManagerHolder.getEncodedPayloadManager().get();
    }

    Enclave enclave = Enclave.create();

    PrivacyHelper privacyHelper = PrivacyHelper.create();
    PayloadDigest payloadDigest = PayloadDigest.create();

    EncodedPayloadManager encodedPayloadManager =
        new EncodedPayloadManagerImpl(enclave, privacyHelper, payloadDigest);
    encodedPayloadManagerHolder.storeInstance(encodedPayloadManager);
    return encodedPayloadManager;
  }
}
