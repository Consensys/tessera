package com.quorum.tessera.enclave.encoder;

import com.quorum.tessera.encryption.Nonce;
import com.quorum.tessera.encryption.PublicKey;
import java.util.List;

public class LegacyEncodedPayload {

  private final PublicKey senderKey;

  private final byte[] cipherText;

  private final Nonce cipherTextNonce;

  private final List<byte[]> recipientBoxes;

  private final Nonce recipientNonce;

  private final List<PublicKey> recipientKeys;

  public LegacyEncodedPayload(
      final PublicKey senderKey,
      final byte[] cipherText,
      final Nonce cipherTextNonce,
      final List<byte[]> recipientBoxes,
      final Nonce recipientNonce,
      final List<PublicKey> recipientKeys) {
    this.senderKey = senderKey;
    this.cipherText = cipherText;
    this.cipherTextNonce = cipherTextNonce;
    this.recipientNonce = recipientNonce;
    this.recipientBoxes = recipientBoxes;
    this.recipientKeys = recipientKeys;
  }

  public PublicKey getSenderKey() {
    return senderKey;
  }

  public byte[] getCipherText() {
    return cipherText;
  }

  public Nonce getCipherTextNonce() {
    return cipherTextNonce;
  }

  public List<byte[]> getRecipientBoxes() {
    return recipientBoxes;
  }

  public Nonce getRecipientNonce() {
    return recipientNonce;
  }

  public List<PublicKey> getRecipientKeys() {
    return recipientKeys;
  }
}
