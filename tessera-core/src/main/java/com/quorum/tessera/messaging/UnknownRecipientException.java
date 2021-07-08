package com.quorum.tessera.messaging;

import com.quorum.tessera.encryption.PublicKey;

public class UnknownRecipientException extends RuntimeException {

  private final PublicKey publicKey;

  public UnknownRecipientException(PublicKey publicKey) {
    this.publicKey = publicKey;
  }

  /** @return the public key of the unknown recipient */
  public PublicKey getPublicKey() {
    return publicKey;
  }
}
