package com.quorum.tessera.passwords;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.quorum.tessera.io.MockSystemAdapter;
import com.quorum.tessera.io.SystemAdapter;
import java.io.ByteArrayInputStream;
import java.io.PrintStream;
import org.junit.Before;
import org.junit.Test;

public class PasswordReaderTest {

  private final SystemAdapter systemAdapter = SystemAdapter.INSTANCE;

  @Before
  public void onSetup() {
    assertThat(systemAdapter).isInstanceOf(MockSystemAdapter.class);

    MockSystemAdapter.class.cast(systemAdapter).setErrPrintStream(mock(PrintStream.class));
    MockSystemAdapter.class.cast(systemAdapter).setOutPrintStream(mock(PrintStream.class));
  }

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
