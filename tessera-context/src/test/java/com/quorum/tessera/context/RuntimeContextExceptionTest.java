package com.quorum.tessera.context;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RuntimeContextExceptionTest {

    @Test
    public void createWithCause() {
        Throwable cause = new Throwable("Ouch that's gonna smart!!");

        RuntimeContextException exception = new RuntimeContextException(cause);

        assertThat(exception.getCause()).isSameAs(cause);
    }
}
