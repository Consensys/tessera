package com.quorum.tessera.encryption;

import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Stream;

public abstract class BaseKey implements Key {

  private final byte[] keyBytes;

  protected BaseKey(byte[] keyBytes) {
    this.keyBytes = keyBytes;
  }

  @Override
  public final byte[] getKeyBytes() {
    return keyBytes;
  }

  @Override
  public String encodeToBase64() {
    return Base64.getEncoder().encodeToString(keyBytes);
  }

  @Override
  public final boolean equals(Object arg0) {
    return getClass().isInstance(arg0)
        && Arrays.equals(keyBytes, getClass().cast(arg0).getKeyBytes());
  }

  @Override
  public final int hashCode() {
    return Arrays.hashCode(keyBytes);
  }

  @Override
  public String toString() {

    final String typeName =
        Stream.of(getClass())
            .map(Class::getInterfaces)
            .flatMap(Stream::of)
            .map(Class::getName)
            .findFirst()
            .get();

    // we use Object.hashCode to protect against accidentally printing/logging a value derived from
    // the raw bytes
    // a side effect of this is 2 instances with the same underlying bytes will have different
    // toString values
    return typeName + "@" + Integer.toHexString(super.hashCode());
  }
}
