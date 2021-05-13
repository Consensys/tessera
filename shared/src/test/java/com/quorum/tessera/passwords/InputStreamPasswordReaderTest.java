package com.quorum.tessera.passwords;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import org.junit.Test;

public class InputStreamPasswordReaderTest {

  @Test
  public void inputDataIsFoundInStream() {

    final byte[] inputStreamBytes = ("TRY1" + System.lineSeparator()).getBytes();

    final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(inputStreamBytes);

    final PasswordReader passwordReader = new InputStreamPasswordReader(byteArrayInputStream);

    final char[] password = passwordReader.readPasswordFromConsole();

    assertThat(String.valueOf(password)).isEqualTo("TRY1");
  }

  @Test
  public void testNoLineFoundInStream() {
    final byte[] inputStreamBytes = new byte[0];
    final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(inputStreamBytes);
    final PasswordReader passwordReader = new InputStreamPasswordReader(byteArrayInputStream);

    final char[] password = passwordReader.readPasswordFromConsole();
    assertThat(String.valueOf(password)).isEmpty();
  }
}
