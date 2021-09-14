package com.quorum.tessera.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PrivacyGroupDeleteRequest {

  @Schema(
      description = "id identifying the privacy group to delete",
      type = "string",
      format = "base64")
  @NotNull
  @Size(min = 1)
  private String privacyGroupId;

  @Schema(
      description = "public key identifying the sender of the request",
      type = "string",
      format = "base64")
  private String from;

  public String getPrivacyGroupId() {
    return privacyGroupId;
  }

  public void setPrivacyGroupId(String privacyGroupId) {
    this.privacyGroupId = privacyGroupId;
  }

  public String getFrom() {
    return from;
  }

  public void setFrom(String from) {
    this.from = from;
  }
}
