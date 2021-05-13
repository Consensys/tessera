package com.quorum.tessera.encryption;

public interface PublicKey extends Key {

  static PublicKey from(byte[] data) {
    return new PublicKeyImpl(data);
  }
}
