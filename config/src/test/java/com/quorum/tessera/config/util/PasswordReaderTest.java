package com.quorum.tessera.config.util;

import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class PasswordReaderTest {

    @Test
    public void passwordsNotMatchingCausesRetry() {

        final byte[] systemInBytes = (
            "TRY1" + System.lineSeparator() +
            "TRY2" + System.lineSeparator() +
            "TRY3" + System.lineSeparator() +
            "TRY3" + System.lineSeparator()
        ).getBytes();

        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(systemInBytes);

        final PasswordReader passwordReader = new InputStreamPasswordReader(byteArrayInputStream);

        final String password = passwordReader.requestUserPassword();

        assertThat(password).isEqualTo("TRY3");

    }

}
