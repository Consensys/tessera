package com.quorum.tessera.config.cli;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NoTesseraCmdArgsExceptionTest {

    @Test
    public void defaultConstructor() {
        NoTesseraCmdArgsException exception = new NoTesseraCmdArgsException();
        assertThat(exception).isNotNull();
    }

}
