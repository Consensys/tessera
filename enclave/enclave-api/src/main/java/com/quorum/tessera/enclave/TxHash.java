package com.quorum.tessera.enclave;

import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Stream;

public class TxHash {

  private final byte[] bytes;

  public TxHash(byte[] hashBytes) {
    this.bytes = hashBytes;
  }

  public TxHash(String b64Hash) {
    this(Base64.getDecoder().decode(b64Hash));
  }

  public String encodeToBase64() {
    return Base64.getEncoder().encodeToString(bytes);
  }

  public byte[] getBytes() {
    return bytes;
  }

  public static TxHash from(byte[] data) {
    return new TxHash(data);
  }

  @Override
  public final boolean equals(Object arg0) {
    return getClass().isInstance(arg0) && Arrays.equals(bytes, getClass().cast(arg0).getBytes());
  }

  @Override
  public final int hashCode() {
    return Arrays.hashCode(bytes);
  }

  @Override
  public final String toString() {

    final String typeName = Stream.of(getClass()).map(Class::getSimpleName).findFirst().get();

    return String.format("%s[%s]", typeName, encodeToBase64());
  }
}
