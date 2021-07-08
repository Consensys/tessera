package com.quorum.tessera.enclave;

import org.bouncycastle.jcajce.provider.digest.SHA3;

public class DefaultPayloadDigest implements PayloadDigest {
  @Override
  public byte[] digest(byte[] cipherText) {
    final SHA3.DigestSHA3 digestSHA3 = new SHA3.Digest512();
    return digestSHA3.digest(cipherText);
  }
}
