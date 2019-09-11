package com.quorum.tessera.cli;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CLIExceptionCapturerTest {

    private CLIExceptionCapturer capturer = new CLIExceptionCapturer();

    @Test
    public void exceptionIsRetrievableAfterInvocation() {
        final Exception testException = new Exception();

        final int exitCode = capturer.handleExecutionException(testException, null, null);

        assertThat(exitCode).isEqualTo(0);
        assertThat(capturer.getThrown()).isSameAs(testException);
    }
}
