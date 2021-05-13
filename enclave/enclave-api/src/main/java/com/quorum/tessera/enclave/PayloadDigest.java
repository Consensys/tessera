package com.quorum.tessera.enclave;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.config.ClientMode;
import com.quorum.tessera.config.Config;
import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.bouncycastle.jcajce.provider.digest.SHA512;

public interface PayloadDigest {

  byte[] digest(byte[] cipherText);

  class Default implements PayloadDigest {
    @Override
    public byte[] digest(byte[] cipherText) {
      final SHA3.DigestSHA3 digestSHA3 = new SHA3.Digest512();
      return digestSHA3.digest(cipherText);
    }
  }

  class SHA512256 implements PayloadDigest {
    @Override
    public byte[] digest(byte[] cipherText) {
      final SHA512.DigestT256 digestSHA512256 = new SHA512.DigestT256();
      return digestSHA512256.digest(cipherText);
    }
  }

  static PayloadDigest create(Config config) {
    return ServiceLoaderUtil.load(PayloadDigest.class)
        .orElseGet(
            () -> {
              if (config.getClientMode() == ClientMode.ORION) return new SHA512256();
              return new Default();
            });
  }
}
