package com.github.nexus.nacl;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class KeyExceptionTest {

    public KeyExceptionTest() {
    }

    @Test
    public void createInstance() {
        final Exception cause = new Exception("OUCH");
        final String message = "HELLOW";
        KeyException exception = new KeyException(message, cause);
        assertThat(exception)
                .hasCause(cause)
                .hasMessage(message);
    }

    @Test
    public void createInstanceWithNullMessage() {
        final Exception cause = new Exception("OUCH");
        KeyException exception = new KeyException(null, cause);
        assertThat(exception).hasCause(cause);
        assertThat(exception.getMessage()).isNull();
    }

    @Test
    public void createInstanceWithNullMessageAndNullCause() {

        KeyException exception = new KeyException(null, null);
        assertThat(exception).hasNoCause();
        assertThat(exception.getMessage()).isNull();
    }
    
    @Test
    public void createInstanceNullCause() {

        final String message = "HELLOW";
        KeyException exception = new KeyException(message, null);
        assertThat(exception)
                .hasNoCause()
                .hasMessage(message);
    }
}
