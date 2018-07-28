package com.quorum.tessera.socket;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class InputStreamUtilsTest {

    @Test
    public void instanceCanBeConstructed() {
        assertThat(new InputStreamUtils()).isNotNull();
    }

    @Test
    public void bytesIsLessThanBufferSize() throws IOException {

        final InputStream is = new ByteArrayInputStream(new byte[]{1, 2, 3, 4, 5});

        final byte[] output = InputStreamUtils.readAllBytes(is);

        assertThat(output).containsExactly(new byte[]{1, 2, 3, 4, 5});

    }

    @Test
    public void bytesIsMoreThanBufferSize() throws IOException {

        final byte[] input = new byte[10000];
        Arrays.fill(input, (byte)15);

        final InputStream is = new ByteArrayInputStream(input);

        final byte[] output = InputStreamUtils.readAllBytes(is);

        assertThat(output).hasSize(10000);

    }


}
