package com.quorum.tessera.cli;

import org.junit.Test;
import picocli.CommandLine;

import java.io.PrintWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class CLIExceptionCapturerTest {

    private CLIExceptionCapturer capturer = new CLIExceptionCapturer();

    @Test
    public void exceptionIsRetrievableAfterInvocation() {
        final Exception testException = new Exception();

        final int exitCode = capturer.handleExecutionException(testException, null, null);

        assertThat(exitCode).isEqualTo(0);
        assertThat(capturer.getThrown()).isSameAs(testException);
    }

    @Test
    public void exceptionWithCauseRetrievable() {
        final Exception cause = new Exception();
        final CommandLine.ParameterException ex =
                new CommandLine.ParameterException(mock(CommandLine.class), "", cause);

        capturer.handleParseException(ex, new String[0]);

        assertThat(capturer.getThrown()).isSameAs(cause);
    }

    @Test
    public void exceptionWithoutCausePrintsUsage() {
        final CommandLine cmd = mock(CommandLine.class);
        final PrintWriter writer = mock(PrintWriter.class);
        when(cmd.getErr()).thenReturn(writer);

        final CommandLine.ParameterException ex = new CommandLine.ParameterException(cmd, "test-message", null);

        capturer.handleParseException(ex, new String[0]);

        verify(cmd).getErr();
        verify(cmd).usage(writer);
        verify(writer).println("test-message");
    }
}
