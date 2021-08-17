package com.quorum.tessera.p2p.resend;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

/**
 * Model representation of a JSON body on incoming HTTP requests. Used when a request is received to
 * resend existing transactions. Contains:
 *
 * <ul>
 *   <li>the public key who is a recipient
 *   <li>the resend type, whether to send a single transaction or all transactions for the given key
 *   <li>the transaction hash to resend in the case the resend type is for an individual transaction
 * </ul>
 */
public class ResendRequest {

  @Pattern(regexp = "^(ALL|INDIVIDUAL)$")
  @Schema(required = true)
  private String type;

  @Schema(
      description = "resend transactions involving this public key",
      required = true,
      format = "base64")
  private String publicKey;

  @Schema(description = "hash of encoded transaction (INDIVIDUAL only)", format = "base64")
  private String key;

  public String getType() {
    return type;
  }

  public void setType(final String type) {
    this.type = type;
  }

  public String getPublicKey() {
    return publicKey;
  }

  public void setPublicKey(final String publicKey) {
    this.publicKey = publicKey;
  }

  public String getKey() {
    return key;
  }

  public void setKey(final String key) {
    this.key = key;
  }
}
