package com.quorum.tessera.cli;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CliResultTest {

    @Test
    public void isSuppressStartup() {
        boolean expected = false;
        CliResult result = new CliResult(0, expected, null);

        assertThat(result.isSuppressStartup()).isEqualTo(expected);
    }
}
