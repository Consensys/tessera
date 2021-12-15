package com.quorum.tessera.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PrivacyGroupRetrieveRequest {

  @Schema(
      description = "id identifying the privacy group to retrieve",
      type = "string",
      format = "base64")
  @NotNull
  @Size(min = 1)
  private String privacyGroupId;

  public String getPrivacyGroupId() {
    return privacyGroupId;
  }

  public void setPrivacyGroupId(String privacyGroupId) {
    this.privacyGroupId = privacyGroupId;
  }
}
