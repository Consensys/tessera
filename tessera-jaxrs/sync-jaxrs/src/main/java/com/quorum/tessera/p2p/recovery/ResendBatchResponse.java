package com.quorum.tessera.p2p.recovery;

import io.swagger.v3.oas.annotations.media.Schema;

/** Resend batch response. */
public class ResendBatchResponse {

  @Schema(description = "count of total transactions being resent")
  private Long total;

  public ResendBatchResponse() {}

  public ResendBatchResponse(Long total) {
    this.total = total;
  }

  public Long getTotal() {
    return total;
  }

  public void setTotal(Long total) {
    this.total = total;
  }
}
