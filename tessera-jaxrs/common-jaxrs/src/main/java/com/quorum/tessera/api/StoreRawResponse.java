package com.quorum.tessera.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.xml.bind.annotation.XmlInlineBinaryData;

/**
 * Model representation of a JSON body on incoming HTTP requests
 *
 * <p>A response to a {@link StoreRawRequest} after the raw transaction has been saved
 */
public class StoreRawResponse {

  @Schema(type = "string", format = "base64", description = "hash of encrypted payload")
  @XmlInlineBinaryData
  private byte[] key;

  public StoreRawResponse(byte[] key) {
    this.key = key;
  }

  public StoreRawResponse() {}

  public byte[] getKey() {
    return key;
  }

  public void setKey(byte[] key) {
    this.key = key;
  }
}
