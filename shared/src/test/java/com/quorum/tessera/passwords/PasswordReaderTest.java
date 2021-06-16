package com.quorum.tessera.passwords;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import org.junit.Test;

public class PasswordReaderTest {

  @Test
  public void passwordsNotMatchingCausesRetry() {
    final byte[] systemInBytes =
        ("TRY1"
                + System.lineSeparator()
                + "TRY2"
                + System.lineSeparator()
                + "TRY3"
                + System.lineSeparator()
                + "TRY3"
                + System.lineSeparator())
            .getBytes();

    final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(systemInBytes);

    final PasswordReader passwordReader = new InputStreamPasswordReader(byteArrayInputStream);

    final char[] password = passwordReader.requestUserPassword();

    assertThat(String.valueOf(password)).isEqualTo("TRY3");
  }
}
