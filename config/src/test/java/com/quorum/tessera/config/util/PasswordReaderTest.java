package com.quorum.tessera.config.util;

import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class PasswordReaderTest {

    @Test
    public void nullConsoleReadsFromInputStream() {

        final PasswordReader passwordReader = new PasswordReader(null, new ByteArrayInputStream("TEST".getBytes()));

        final String readValue = passwordReader.readPassword();

        assertThat(readValue).isEqualTo("TEST");

    }

}
