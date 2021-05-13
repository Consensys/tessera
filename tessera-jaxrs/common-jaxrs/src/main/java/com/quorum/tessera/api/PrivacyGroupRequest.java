package com.quorum.tessera.api;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

public class PrivacyGroupRequest {

  @ArraySchema(
      arraySchema =
          @Schema(description = "public keys identifying the members of the privacy group"),
      schema = @Schema(format = "base64"))
  private String[] addresses;

  @Schema(
      description = "public key identifying the sender of the request",
      type = "string",
      format = "base64")
  private String from;

  @Schema(description = "name of the privacy group", type = "string")
  private String name;

  @Schema(description = "description of the privacy group", type = "string")
  private String description;

  @Hidden private String seed;

  public PrivacyGroupRequest() {}

  public String[] getAddresses() {
    return addresses;
  }

  public void setAddresses(String[] addresses) {
    this.addresses = addresses;
  }

  public String getFrom() {
    return from;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getSeed() {
    return seed;
  }

  public void setSeed(String seed) {
    this.seed = seed;
  }
}
