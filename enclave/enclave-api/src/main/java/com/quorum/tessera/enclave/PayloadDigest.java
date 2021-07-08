package com.quorum.tessera.enclave;

import com.quorum.tessera.config.ClientMode;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import java.util.ServiceLoader;

public interface PayloadDigest {

  byte[] digest(byte[] cipherText);

  static PayloadDigest create() {

    Config config = ConfigFactory.create().getConfig();

    // FIXME:
    final Class implType;
    if (config.getClientMode() == ClientMode.ORION) {
      implType = SHA512256PayloadDigest.class;
    } else {
      implType = DefaultPayloadDigest.class;
    }
    return ServiceLoader.load(PayloadDigest.class).stream()
        .filter(payloadDigestProvider -> payloadDigestProvider.type() == implType)
        .map(ServiceLoader.Provider::get)
        .findFirst()
        .get();
  }
}
