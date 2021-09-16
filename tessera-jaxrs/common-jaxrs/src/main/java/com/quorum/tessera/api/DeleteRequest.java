package com.quorum.tessera.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Model representation of a JSON body on incoming HTTP requests
 *
 * <p>Contains information that is used to delete a transaction
 */
public class DeleteRequest {

  @Schema(
      description = "hash indicating encrypted payload to delete from database",
      format = "base64")
  @Size(min = 1)
  @NotNull
  private String key;

  public String getKey() {
    return key;
  }

  public void setKey(final String key) {
    this.key = key;
  }
}
