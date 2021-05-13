package com.quorum.tessera.thirdparty.model;

import io.swagger.v3.oas.annotations.media.Schema;

// TODO(cjh) just used for swagger generation - should be used in the actual jaxrs methods
public class Key {

  @Schema(description = "public key", format = "base64")
  private String key;

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }
}
