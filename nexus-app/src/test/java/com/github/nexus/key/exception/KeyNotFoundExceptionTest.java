package com.github.nexus.key.exception;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyNotFoundExceptionTest {

    public KeyNotFoundExceptionTest() {
    }

    @Test
    public void constructWithMessage() {

        String message = "Some punk's busted up my ride!!";

        KeyNotFoundException keyNotFoundException = new KeyNotFoundException(message);

        assertThat(keyNotFoundException.getMessage()).isEqualTo(message);


    }

    @Test
    public void constructWithMessageAndCause() {

        String message = "Some punk's busted up my ride!!";
        Throwable cause = new Exception("OUCH");
        KeyNotFoundException keyNotFoundException = new KeyNotFoundException(message,cause);

        assertThat(keyNotFoundException.getMessage()).isEqualTo(message);
        assertThat(keyNotFoundException.getCause()).isSameAs(cause);
    }


    @Test
    public void constructWithCause() {

        Throwable cause = new Exception("OUCH");
        KeyNotFoundException keyNotFoundException = new KeyNotFoundException(cause);

        assertThat(keyNotFoundException.getMessage()).isEqualTo("java.lang.Exception: OUCH");
        assertThat(keyNotFoundException.getCause()).isSameAs(cause);

    }
}
