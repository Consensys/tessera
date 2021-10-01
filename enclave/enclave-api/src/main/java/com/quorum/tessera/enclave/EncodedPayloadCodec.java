package com.quorum.tessera.enclave;

public enum EncodedPayloadCodec {
  LEGACY,
  UNSUPPORTED;

  public static EncodedPayloadCodec current() {
    return LEGACY;
  }
}
