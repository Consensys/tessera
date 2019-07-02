package com.quorum.tessera.passwords;

import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class InputStreamPasswordReaderTest {

    @Test
    public void inputDataIsFoundInStream() {

        final byte[] inputStreamBytes = ("TRY1" + System.lineSeparator()).getBytes();

        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(inputStreamBytes);

        final PasswordReader passwordReader = new InputStreamPasswordReader(byteArrayInputStream);

        final String password = passwordReader.readPasswordFromConsole();

        assertThat(password).isEqualTo("TRY1");
    }

    @Test
    public void testNoLineFoundInStream() {
        final byte[] inputStreamBytes = new byte[0];
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(inputStreamBytes);
        final PasswordReader passwordReader = new InputStreamPasswordReader(byteArrayInputStream);

        final String password = passwordReader.readPasswordFromConsole();
        assertThat(password).isEmpty();
    }
}
