package com.quorum.tessera.socket;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TesseraSocketExceptionTest {

    @Test
    public void createWithCause() {

        final UnsupportedOperationException cause = new UnsupportedOperationException("OUCH");
        final TesseraSocketException exception = new TesseraSocketException(cause);

        assertThat(exception).hasCause(cause);

    }

}
