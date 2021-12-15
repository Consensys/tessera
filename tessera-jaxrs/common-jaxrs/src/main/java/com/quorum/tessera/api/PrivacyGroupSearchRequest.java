package com.quorum.tessera.api;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public class PrivacyGroupSearchRequest {

  @ArraySchema(
      arraySchema = @Schema(description = "public keys of the members of the privacy group"),
      schema = @Schema(format = "base64"))
  @NotNull
  private String[] addresses;

  public String[] getAddresses() {
    return addresses;
  }

  public void setAddresses(String[] addresses) {
    this.addresses = addresses;
  }
}
