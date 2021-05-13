package com.quorum.tessera.encryption;

import java.util.Arrays;

/** A set of random bytes intended for one time use to encrypt a message */
public class Nonce {

  private final byte[] nonceBytes;

  public Nonce(final byte[] nonceBytes) {
    this.nonceBytes = Arrays.copyOf(nonceBytes, nonceBytes.length);
  }

  public byte[] getNonceBytes() {
    return Arrays.copyOf(nonceBytes, nonceBytes.length);
  }

  @Override
  public boolean equals(final Object o) {
    return (o instanceof Nonce) && Arrays.equals(nonceBytes, ((Nonce) o).nonceBytes);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(nonceBytes);
  }

  @Override
  public String toString() {
    // we use Object.hashCode to protect against accidentally printing/logging a value derived from
    // the raw bytes
    // a side effect of this is 2 instances with the same underlying bytes will have different
    // toString values
    return getClass().getName() + "@" + Integer.toHexString(super.hashCode());
  }
}
