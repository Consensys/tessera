package com.quorum.tessera.encryption;

import java.io.Serializable;

/** A generic key that represents many different types of keys used for encryption */
public interface Key extends Serializable {

  /**
   * Retrieve the underlying byte array this key represents
   *
   * @return the byte values of the key
   */
  byte[] getKeyBytes();

  /**
   * Encode this keys bytes to its Base64 representation
   *
   * @return the Base64 representation of the key
   */
  String encodeToBase64();
}
