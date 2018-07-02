package com.github.nexus.nacl;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class NaclExceptionTest {

    public NaclExceptionTest() {
    }

    @Test
    public void createInstance() {
        final String message = "HELLOW";
        NaclException exception = new NaclException(message);
        assertThat(exception)
                .hasNoCause()
                .hasMessage(message);
    }

    @Test
    public void createInstanceWithNullMessage() {
        NaclException exception = new NaclException(null);
        assertThat(exception)
                .hasNoCause();
        assertThat(exception.getMessage())
                .isNull();
    }

}
