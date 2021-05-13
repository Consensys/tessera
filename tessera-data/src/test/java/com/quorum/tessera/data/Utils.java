package com.quorum.tessera.data;

import java.util.UUID;

public interface Utils {

  static MessageHash createHash() {
    return new MessageHash(randomBytes());
  }

  static String createHashStr() {
    return new String(randomBytes());
  }

  static byte[] randomBytes() {
    return UUID.randomUUID().toString().getBytes();
  }

  static byte[] cipherText() {
    return randomBytes();
  }

  static byte[] cipherTextNonce() {
    return randomBytes();
  }
}
