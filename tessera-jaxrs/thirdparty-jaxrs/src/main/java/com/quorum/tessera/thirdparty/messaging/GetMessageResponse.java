package com.quorum.tessera.thirdparty.messaging;

import io.swagger.v3.oas.annotations.media.Schema;

public class GetMessageResponse {

  @Schema(
      description = "Public key identifying the message sender",
      type = "string",
      format = "base64")
  private String from;

  @Schema(description = "Message contents", type = "string", format = "base64")
  private String content;

  public String getFrom() {
    return from;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }
}
