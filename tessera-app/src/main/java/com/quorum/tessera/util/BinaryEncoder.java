package com.quorum.tessera.util;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Encodes bytes to the binary form expected by other nodes on the network
 */
public interface BinaryEncoder {

    /**
     * Converts a byte array to its binary form, which takes the following:
     * 8 bytes to describe the number of bytes long the data is
     * the bytes of the data
     *
     * @param data the input array to encode
     * @return the encoded byte array
     */
    default byte[] encodeField(final byte[] data) {
        return ByteBuffer
            .allocate(Long.BYTES + data.length)
            .putLong(data.length)
            .put(data)
            .array();
    }

    /**
     * Converts a 2d-byte array to its binary form, which takes the following:
     *
     * 8 bytes to describe the number of elements
     * and for each element:
     * - 8 bytes to describe the number of bytes long the element is
     * - the bytes of the element
     *
     * @param data the input array to encode
     * @return the encoded byte array
     */
    default byte[] encodeArray(final byte[][] data) {
        return encodeArray(Stream.of(data).collect(toList()));
    }

    /**
     * Converts a list of byte arrays to its binary form, which takes the following:
     *
     * 8 bytes to describe the number of elements
     * and for each element:
     * - 8 bytes to describe the number of bytes long the element is
     * - the bytes of the element
     *
     * @param data the input array to encode
     * @return the encoded byte array
     */
    default byte[] encodeArray(final List<byte[]> data) {
        final int numberOfElements = data.size();

        if(numberOfElements == 0) {
            return new byte[Long.BYTES];
        }

        final Long totalLengthOfElements = data
            .stream()
            .mapToLong(element -> element.length)
            .sum();

        final ByteBuffer buffer = ByteBuffer
            .allocate(Long.BYTES + Long.BYTES*numberOfElements + totalLengthOfElements.intValue());

        buffer.putLong(numberOfElements);

        for(final byte[] element : data) {
            buffer.putLong(element.length);
            buffer.put(element);
        }

        return buffer.array();
    }

}
