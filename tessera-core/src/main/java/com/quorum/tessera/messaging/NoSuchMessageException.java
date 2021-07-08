package com.quorum.tessera.messaging;

import com.quorum.tessera.data.MessageHash;

public class NoSuchMessageException extends Exception {

  private final MessageHash messageHash;

  public NoSuchMessageException(MessageHash messageHash) {
    this.messageHash = messageHash;
  }

  public MessageHash getMessageHash() {
    return messageHash;
  }
}
