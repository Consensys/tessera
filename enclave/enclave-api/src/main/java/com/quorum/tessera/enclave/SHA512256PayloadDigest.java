package com.quorum.tessera.enclave;

import org.bouncycastle.jcajce.provider.digest.SHA512;

public class SHA512256PayloadDigest implements PayloadDigest {
  @Override
  public byte[] digest(byte[] cipherText) {
    final SHA512.DigestT256 digestSHA512256 = new SHA512.DigestT256();
    return digestSHA512256.digest(cipherText);
  }
}
