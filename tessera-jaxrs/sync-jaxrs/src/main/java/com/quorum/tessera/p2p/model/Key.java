package com.quorum.tessera.p2p.model;

import io.swagger.v3.oas.annotations.media.Schema;

// TODO(cjh) just used for swagger generation - should be used in the actual jaxrs methods
public class Key {
  @Schema(description = "known public key of peer", format = "base64")
  private String key;

  @Schema(description = "public key's corresponding peer url")
  private String url;

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }
}
