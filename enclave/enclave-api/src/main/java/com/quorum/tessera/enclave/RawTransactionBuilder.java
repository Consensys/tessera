package com.quorum.tessera.enclave;

import com.quorum.tessera.encryption.Nonce;
import com.quorum.tessera.encryption.PublicKey;

public class RawTransactionBuilder {

  private RawTransactionBuilder() {}

  public static RawTransactionBuilder create() {
    return new RawTransactionBuilder();
  }

  private byte[] encryptedPayload;

  private byte[] encryptedKey;

  private byte[] nonce;

  private PublicKey from;

  public RawTransactionBuilder withEncryptedPayload(final byte[] encryptedPayload) {
    this.encryptedPayload = encryptedPayload;
    return this;
  }

  public RawTransactionBuilder withEncryptedKey(final byte[] encryptedKey) {
    this.encryptedKey = encryptedKey;
    return this;
  }

  public RawTransactionBuilder withFrom(final PublicKey from) {
    this.from = from;
    return this;
  }

  public RawTransactionBuilder withNonce(final byte[] nonce) {
    this.nonce = nonce;
    return this;
  }

  public RawTransaction build() {
    return new RawTransaction(encryptedPayload, encryptedKey, new Nonce(nonce), from);
  }
}
