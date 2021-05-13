package com.quorum.tessera.p2p.model;

import io.swagger.v3.oas.annotations.media.Schema;

// TODO(cjh) just used for swagger generation - should be used in the actual jaxrs methods
public class Peer {
  @Schema(description = "peer's server url")
  private String url;

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }
}
