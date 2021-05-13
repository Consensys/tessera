package com.quorum.tessera.enclave;

import java.nio.ByteBuffer;
import java.util.List;

/** Encodes bytes to the binary form expected by other nodes on the network */
public interface BinaryEncoder {

  /**
   * Converts a byte array to its binary form, which takes the following: 8 bytes to describe the
   * number of bytes long the data is the bytes of the data
   *
   * @param data the input array to encode
   * @return the encoded byte array
   */
  default byte[] encodeField(final byte[] data) {
    return ByteBuffer.allocate(Long.BYTES + data.length).putLong(data.length).put(data).array();
  }

  /**
   * Converts a list of byte arrays to its binary form, which takes the following:
   *
   * <p>8 bytes to describe the number of elements and for each element: - 8 bytes to describe the
   * number of bytes long the element is - the bytes of the element
   *
   * @param data the input array to encode
   * @return the encoded byte array
   */
  default byte[] encodeArray(final List<byte[]> data) {
    final byte[] elements =
        data.stream()
            .map(this::encodeField)
            .reduce(
                new byte[0],
                (a, b) -> {
                  final byte[] concat = new byte[a.length + b.length];
                  System.arraycopy(a, 0, concat, 0, a.length);
                  System.arraycopy(b, 0, concat, a.length, b.length);
                  return concat;
                });

    return ByteBuffer.allocate(Long.BYTES + elements.length)
        .putLong(data.size())
        .put(elements)
        .array();
  }
}
