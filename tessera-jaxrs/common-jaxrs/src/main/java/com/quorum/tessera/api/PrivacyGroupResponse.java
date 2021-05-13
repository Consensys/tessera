package com.quorum.tessera.api;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

public class PrivacyGroupResponse {

  @Schema(description = "id of the privacy group", type = "string", format = "base64")
  private String privacyGroupId;

  @Schema(description = "name of the privacy group", type = "string")
  private String name;

  @Schema(description = "description of the privacy group", type = "string")
  private String description;

  @Schema(
      description = "type of the privacy group",
      type = "string",
      allowableValues = {"PANTHEON", "LEGACY"})
  private String type;

  @ArraySchema(
      arraySchema = @Schema(description = "public keys of the members of the privacy group"),
      schema = @Schema(format = "base64"))
  private String[] members;

  public PrivacyGroupResponse(
      String privacyGroupId, String name, String description, String type, String[] members) {
    this.privacyGroupId = privacyGroupId;
    this.name = name;
    this.description = description;
    this.type = type;
    this.members = members;
  }

  public PrivacyGroupResponse() {}

  public String getPrivacyGroupId() {
    return privacyGroupId;
  }

  public void setPrivacyGroupId(String privacyGroupId) {
    this.privacyGroupId = privacyGroupId;
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

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String[] getMembers() {
    return members;
  }

  public void setMembers(String[] members) {
    this.members = members;
  }
}
