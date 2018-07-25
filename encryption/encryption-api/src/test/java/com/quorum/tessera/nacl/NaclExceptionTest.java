package com.quorum.tessera.nacl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class NaclExceptionTest {

    @Test
    public void createInstance() {
        final String message = "HELLOW";
        final NaclException exception = new NaclException(message);

        assertThat(exception).hasNoCause().hasMessage(message);
    }

    @Test
    public void createInstanceWithNullMessage() {
        final NaclException exception = new NaclException(null);

        assertThat(exception).hasNoCause();
        assertThat(exception.getMessage()).isNull();
    }

}
