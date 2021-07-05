package com.quorum.tessera.thirdparty.messaging;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public class MessageListResponse {

  @ArraySchema(schema = @Schema(description = "A message identifier", type = "string"))
  private List<String> messageIds;

  public List<String> getMessageIds() {
    return messageIds;
  }

  public void setMessageIds(List<String> messageIds) {
    this.messageIds = messageIds;
  }
}
