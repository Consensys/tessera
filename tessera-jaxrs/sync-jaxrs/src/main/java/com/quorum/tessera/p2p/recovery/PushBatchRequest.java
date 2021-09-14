package com.quorum.tessera.p2p.recovery;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.xml.bind.annotation.XmlMimeType;
import java.util.List;

/** Model representation of a list of encoded payloads */
public class PushBatchRequest {

  @ArraySchema(
      schema = @Schema(description = "list of encoded payloads", type = "string", format = "byte"))
  @XmlMimeType("base64Binary")
  private List<byte[]> encodedPayloads;

  public PushBatchRequest() {}

  public PushBatchRequest(List<byte[]> encodedPayloads) {
    this.encodedPayloads = encodedPayloads;
  }

  public List<byte[]> getEncodedPayloads() {
    return encodedPayloads;
  }

  public void setEncodedPayloads(List<byte[]> encodedPayloads) {
    this.encodedPayloads = encodedPayloads;
  }
}
