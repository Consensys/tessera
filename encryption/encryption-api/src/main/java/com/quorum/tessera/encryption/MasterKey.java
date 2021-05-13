package com.quorum.tessera.encryption;

public interface MasterKey extends Key {

  static MasterKey from(byte[] data) {
    return new MasterKeyImpl(data);
  }
}
