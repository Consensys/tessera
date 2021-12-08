package com.quorum.tessera.enclave;

import java.util.*;

/** Encodes and decodes a {@link EncodedPayload} to and from its binary representation */
public interface PayloadEncoder {

  /**
   * Encodes the payload to a byte array
   *
   * @param payload the payload to encode
   * @return the byte array representing the encoded payload
   */
  byte[] encode(EncodedPayload payload);

  /**
   * Decodes a byte array back into an encrypted payload
   *
   * @param input The byte array to decode into an EncodedPayload
   * @return the decoded payload
   */
  EncodedPayload decode(byte[] input);

  EncodedPayloadCodec encodedPayloadCodec();

  static PayloadEncoder create(EncodedPayloadCodec encodedPayloadCodec) {
    return ServiceLoader.load(PayloadEncoder.class).stream()
        .map(ServiceLoader.Provider::get)
        .filter(e -> e.encodedPayloadCodec() == encodedPayloadCodec)
        .reduce(
            (l, r) -> {
              throw new IllegalStateException(
                  "Resolved multiple encoders for codec " + encodedPayloadCodec);
            })
        .orElseThrow(
            () -> new IllegalStateException("No encoder found for " + encodedPayloadCodec));
  }
}
