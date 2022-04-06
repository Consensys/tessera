package com.quorum.tessera.p2p.model;

import io.swagger.v3.oas.annotations.media.Schema;

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
