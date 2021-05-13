package com.quorum.tessera.encryption;

public interface SharedKey extends Key {

  static SharedKey from(byte[] data) {
    return new SharedKeyImpl(data);
  }
}
