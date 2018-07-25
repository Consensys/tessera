
package com.quorum.tessera.util.exception;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class DecodingExceptionTest {

    @Test
    public void constructWithCause() {

        final Throwable cause = new Exception("OUCH");
        final DecodingException decodingException = new DecodingException(cause);

        Assertions.assertThat(decodingException.getMessage()).isEqualTo("java.lang.Exception: OUCH");
        Assertions.assertThat(decodingException.getCause()).isSameAs(cause);

    }

}
