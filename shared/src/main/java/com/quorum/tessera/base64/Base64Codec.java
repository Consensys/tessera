package com.quorum.tessera.base64;

import java.util.Base64;

/** An delegate interface for {@link Base64} to allow mocking in tests */
public interface Base64Codec {

  /**
   * Decodes a Base64 encoded string to its constituent bytes
   *
   * @param data the input string that is Base64 encoded
   * @return the decoded input
   * @throws DecodingException if the input is not valid Base64
   */
  default byte[] decode(final String data) {
    try {
      return Base64.getDecoder().decode(data);
    } catch (final IllegalArgumentException ex) {
      throw new DecodingException(ex);
    }
  }

  /**
   * Encodes the given input to a Base64 encoded string Does not trim the end if the input was
   * padded
   *
   * @param data the input data to encode to Base64
   * @return the encoded form of the input
   */
  default String encodeToString(final byte[] data) {
    return Base64.getEncoder().encodeToString(data);
  }

  static Base64Codec create() {
    return new Base64Codec() {};
  }
}
