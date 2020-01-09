package com.quorum.tessera.cli;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CliExceptionTest {

    @Test
    public void createWithMessage() {
        final String message = "OUCH";
        CliException cliException = new CliException(message);
        assertThat(cliException).hasMessage(message);
    }
}
