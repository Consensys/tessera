package com.github.tessera.socket;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NexusSocketExceptionTest {

    @Test
    public void createWithCause() {

        final UnsupportedOperationException cause = new UnsupportedOperationException("OUCH");
        final NexusSocketException exception = new NexusSocketException(cause);

        assertThat(exception).hasCause(cause);

    }

}
