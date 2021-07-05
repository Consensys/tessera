package com.quorum.tessera.thirdparty.messaging;

import io.swagger.v3.oas.annotations.media.Schema;

public class SendMessageRequest {

  @Schema(
      description = "Public key identifying the message sender",
      type = "string",
      format = "base64")
  private String from;

  @Schema(
      description = "Public key identifying the message sender",
      type = "string",
      format = "base64")
  private String to;

  @Schema(
      description = "The content of the message the sender wants the receiver to have",
      type = "string",
      format = "base64")
  private String data;

  public String getFrom() {
    return from;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public String getTo() {
    return to;
  }

  public void setTo(String to) {
    this.to = to;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }
}
