package com.quorum.tessera.messaging;

import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.util.Base64Codec;

public class Message {

  private static final Base64Codec base64Codec = Base64Codec.create();

  private final PublicKey sender;
  private final PublicKey recipient;
  private final byte[] data;

  public Message(PublicKey sender, PublicKey recipient, byte[] data) {
    this.sender = sender;
    this.recipient = recipient;
    this.data = data;
  }

  public PublicKey getSender() {
    return sender;
  }

  public PublicKey getRecipient() {
    return recipient;
  }

  public byte[] getData() {
    return data;
  }

  @Override
  public String toString() {

    return "[\""
        + getSender().encodeToBase64()
        + "\" -> \""
        + getRecipient().encodeToBase64()
        + "\": \""
        + base64Codec.encodeToString(getData())
        + "\"]";
  }
}
