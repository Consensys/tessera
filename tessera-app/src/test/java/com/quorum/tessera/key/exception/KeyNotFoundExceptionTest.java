package com.quorum.tessera.key.exception;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyNotFoundExceptionTest {

    @Test
    public void constructWithMessage() {

        final String message = "Generic message";

        final KeyNotFoundException keyNotFoundException = new KeyNotFoundException(message);

        assertThat(keyNotFoundException.getMessage()).isEqualTo(message);

    }

}
