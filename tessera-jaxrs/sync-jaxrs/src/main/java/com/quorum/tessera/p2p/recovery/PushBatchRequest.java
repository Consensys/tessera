package com.quorum.tessera.p2p.recovery;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlMimeType;
import java.util.List;

/** Model representation of a list of encoded payloads */
public class PushBatchRequest {

  @ArraySchema(
      schema = @Schema(description = "list of encoded payloads", type = "string", format = "byte"))
  @XmlMimeType("base64Binary")
  private List<byte[]> encodedPayloads;

  @Schema(description = "codec to encode/decode the payloads", type = "string")
  @Size(min = 1)
  @NotNull
  @Pattern(regexp = "^LEGACY$", message = "Only LEGACY formatted payloads are supported")
  private String encodedPayloadCodec;

  public PushBatchRequest() {}

  public PushBatchRequest(List<byte[]> encodedPayloads, String encodedPayloadCodec) {
    this.encodedPayloads = encodedPayloads;
    this.encodedPayloadCodec = encodedPayloadCodec;
  }

  public List<byte[]> getEncodedPayloads() {
    return encodedPayloads;
  }

  public void setEncodedPayloads(List<byte[]> encodedPayloads) {
    this.encodedPayloads = encodedPayloads;
  }

  public String getEncodedPayloadCodec() {
    return encodedPayloadCodec;
  }

  public void setEncodedPayloadCodec(String encodedPayloadCodec) {
    this.encodedPayloadCodec = encodedPayloadCodec;
  }
}
