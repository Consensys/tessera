package com.quorum.tessera.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlInlineBinaryData;
import java.util.Optional;

/**
 * Model representation of a JSON body on incoming HTTP requests
 *
 * <p>Used when a new raw transaction is to be created where this node is the sender
 */
public class StoreRawRequest {

  @Schema(
      description = "data to be encrypted and stored",
      required = true,
      type = "string",
      format = "base64")
  @Size(min = 1)
  @NotNull
  @XmlInlineBinaryData
  private byte[] payload;

  @Schema(
      description =
          "public key identifying the key pair that will be used in the encryption; if not set, default used",
      type = "string",
      format = "base64")
  @XmlInlineBinaryData
  private byte[] from;

  @XmlInlineBinaryData
  public byte[] getPayload() {
    return payload;
  }

  public void setPayload(byte[] payload) {
    this.payload = payload;
  }

  public Optional<byte[]> getFrom() {
    return Optional.ofNullable(from);
  }

  public void setFrom(byte[] from) {
    this.from = from;
  }
}
