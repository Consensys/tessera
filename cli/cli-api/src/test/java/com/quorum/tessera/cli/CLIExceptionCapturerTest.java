package com.quorum.tessera.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.io.PrintWriter;
import org.junit.Before;
import org.junit.Test;
import picocli.CommandLine;

public class CLIExceptionCapturerTest {

  private CLIExceptionCapturer capturer;

  @Before
  public void setUp() {
    this.capturer = new CLIExceptionCapturer();
  }

  @Test
  public void captureExecutionException() {
    final CommandLine cmd = mock(CommandLine.class);

    final Exception testException = new Exception();

    final int exitCode = capturer.handleExecutionException(testException, cmd, null);

    assertThat(exitCode).isEqualTo(0);
    assertThat(capturer.getThrown()).isSameAs(testException);
    verifyNoInteractions(cmd);
  }

  @Test
  public void cliExecutionExceptionPrintsUsage() {
    final CommandLine cmd = mock(CommandLine.class);
    final PrintWriter pw = mock(PrintWriter.class);
    when(cmd.getErr()).thenReturn(pw);

    final Exception testException = new CliException("some error");

    final int exitCode = capturer.handleExecutionException(testException, cmd, null);

    assertThat(exitCode).isEqualTo(0);
    assertThat(capturer.getThrown()).isSameAs(testException);

    verify(cmd).getErr();
    verify(cmd).usage(pw);
  }

  @Test
  public void captureAndWrapParseExceptionPrintUsage() {
    final CommandLine cmd = mock(CommandLine.class);
    final PrintWriter pw = mock(PrintWriter.class);
    when(cmd.getErr()).thenReturn(pw);

    final Exception cause = new Exception();
    final CommandLine.ParameterException ex =
        new CommandLine.ParameterException(cmd, "some error", cause);

    final int exitCode = capturer.handleParseException(ex, new String[0]);

    final Exception captured = capturer.getThrown();

    assertThat(exitCode).isEqualTo(0);
    assertThat(captured).isExactlyInstanceOf(CliException.class);
    assertThat(captured).hasMessage("some error");

    verify(cmd).getErr();
    verify(cmd).usage(pw);
  }
}
