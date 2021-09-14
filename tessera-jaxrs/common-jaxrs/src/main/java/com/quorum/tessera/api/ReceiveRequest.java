package com.quorum.tessera.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Model representation of a JSON body on incoming HTTP requests
 *
 * <p>Contains information for retrieving a decrypting payload such as the message hash and the
 * public key to decrypt with
 */
public class ReceiveRequest {

  @Schema(
      description = "hash indicating encrypted payload to retrieve from database",
      format = "base64")
  @Size(min = 1)
  @NotNull
  private String key;

  @Schema(
      description =
          "(optional) public key of recipient of the encrypted payload; used in decryption; if not provided, decryption is attempted with all known recipient keys in turn",
      format = "base64")
  @Size(min = 1)
  private String to;

  @Schema(
      description =
          "(optional) indicates whether the payload is raw; determines which database the payload is retrieved from; possible values\n* true - for pre-stored payloads in the \"raw\" database\n* false (default) - for already sent payloads in \"standard\" database")
  private boolean isRaw = false;

  public String getKey() {
    return key;
  }

  public void setKey(final String key) {
    this.key = key;
  }

  public String getTo() {
    return to;
  }

  public void setTo(final String to) {
    this.to = to;
  }

  public boolean isRaw() {
    return isRaw;
  }

  public void setRaw(boolean raw) {
    isRaw = raw;
  }
}
