
package com.github.tessera.util.exception;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DecodingExceptionTest {

    @Test
    public void constructWithCause() {

        final Throwable cause = new Exception("OUCH");
        final DecodingException decodingException = new DecodingException(cause);

        assertThat(decodingException.getMessage()).isEqualTo("java.lang.Exception: OUCH");
        assertThat(decodingException.getCause()).isSameAs(cause);

    }

}
