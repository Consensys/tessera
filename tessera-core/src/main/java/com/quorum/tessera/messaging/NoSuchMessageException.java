package com.quorum.tessera.messaging;

public class NoSuchMessageException extends RuntimeException {

  private final MessageId messageId;

  public NoSuchMessageException(MessageId messageId) {
    this.messageId = messageId;
  }

  public MessageId getMessageId() {
    return messageId;
  }
}
