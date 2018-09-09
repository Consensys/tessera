package com.quorum.tessera.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BinaryEncoderTest {

    private BinaryEncoder binaryEncoder = new BinaryEncoder() {
    };

    @Test
    public void byteArrayEncodesToBinary() {

        final byte[] array = new byte[]{5, 6, 7};

        final byte[] encoded = binaryEncoder.encodeField(array);

        final byte[] expectedResult = new byte[]{0, 0, 0, 0, 0, 0, 0, 3, 5, 6, 7};

        assertThat(encoded).containsExactly(expectedResult);

    }

    @Test
    public void byteArrayArrayEncodesToBinary() {

        final byte[][] array = new byte[][]{
            new byte[]{5},
            new byte[]{6, 7}
        };

        final byte[] encoded = binaryEncoder.encodeArray(array);

        final byte[] expectedResult = new byte[]{
            0, 0, 0, 0, 0, 0, 0, 2,
            0, 0, 0, 0, 0, 0, 0, 1, 5,
            0, 0, 0, 0, 0, 0, 0, 2, 6, 7
        };

        assertThat(encoded).containsExactly(expectedResult);

    }

    @Test
    public void byteArrayListEncodesToBinary() {

        final List<byte[]> lst = Arrays.asList(
            new byte[]{5},
            new byte[]{6, 7}
        );

        final byte[] encoded = binaryEncoder.encodeArray(lst);

        final byte[] expectedResult = new byte[]{
            0, 0, 0, 0, 0, 0, 0, 2,
            0, 0, 0, 0, 0, 0, 0, 1, 5,
            0, 0, 0, 0, 0, 0, 0, 2, 6, 7
        };

        assertThat(encoded).containsExactly(expectedResult);

    }

    @Test
    public void emptyByteArrayListEncodesToBinary() {

        final List<byte[]> lst = new ArrayList<>();

        final byte[] encoded = binaryEncoder.encodeArray(lst);

        final byte[] expectedResult = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};

        assertThat(encoded).containsExactly(expectedResult);

    }





}
