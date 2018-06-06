package com.github.nexus.util;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public interface BinaryEncoder {

    default byte[] encodeField(final byte[] data) {
        return ByteBuffer
            .allocate(Long.BYTES + data.length)
            .putLong(data.length)
            .put(data)
            .array();
    }

    default byte[] encodeArray(final byte[][] data) {
        return encodeArray(Stream.of(data).collect(toList()));
    }

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
