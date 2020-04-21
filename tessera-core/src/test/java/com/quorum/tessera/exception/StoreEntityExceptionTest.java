package com.quorum.tessera.exception;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StoreEntityExceptionTest {

    @Test
    public void testInit() {
        Exception cause = new Exception("Ouch");
        StoreEntityException ex = new StoreEntityException("Message", cause);
        assertThat(ex).hasCause(cause);
        assertThat(ex).hasMessage("Message");
    }
}
