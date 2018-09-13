package com.quorum.tessera.config.util;

import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class PasswordReaderTest {

    @Test
    public void nullConsoleReadsFromInputStream() {

        final PasswordReader passwordReader = new PasswordReader(null, new ByteArrayInputStream("TEST".getBytes()));

        final String readValue = passwordReader.readPasswordFromConsole();

        assertThat(readValue).isEqualTo("TEST");

    }

    @Test
    public void passwordsNotMatchingCausesRetry() {

        final byte[] systemInBytes = (
            "TRY1" + System.lineSeparator() +
            "TRY2" + System.lineSeparator() +
            "TRY3" + System.lineSeparator() +
            "TRY3" + System.lineSeparator()
        ).getBytes();

        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(systemInBytes);

        final PasswordReader passwordReader = new PasswordReader(null, byteArrayInputStream);

        final String password = passwordReader.requestUserPassword();

        assertThat(password).isEqualTo("TRY3");

    }

}
