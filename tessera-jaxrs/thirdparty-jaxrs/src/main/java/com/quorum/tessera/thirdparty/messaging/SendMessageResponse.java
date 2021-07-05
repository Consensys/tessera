package com.quorum.tessera.thirdparty.messaging;

import io.swagger.v3.oas.annotations.media.Schema;

public class SendMessageResponse {

  @Schema(description = "identifier of the sent message", type = "string")
  private String messageId;

  public String getMessageId() {
    return messageId;
  }

  public void setMessageId(String messageId) {
    this.messageId = messageId;
  }
}
