package com.quorum.tessera.p2p.recovery;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Model representation of a JSON body on incoming HTTP requests. Used when a request is received to
 * resend existing transactions. Contains:
 *
 * <ul>
 *   <li>the public key who is a recipient
 *   <li>the batch size
 * </ul>
 */
public class ResendBatchRequest {

  @Schema(
      description = "resend transactions involving this public key",
      format = "base64",
      required = true)
  private String publicKey;

  @Schema(description = "default value is used if not provided")
  private Integer batchSize;

  public String getPublicKey() {
    return publicKey;
  }

  public void setPublicKey(final String publicKey) {
    this.publicKey = publicKey;
  }

  public Integer getBatchSize() {
    return batchSize;
  }

  public void setBatchSize(Integer batchSize) {
    this.batchSize = batchSize;
  }
}
