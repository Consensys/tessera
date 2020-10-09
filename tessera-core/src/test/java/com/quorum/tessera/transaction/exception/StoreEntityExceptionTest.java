package com.quorum.tessera.transaction.exception;

import com.quorum.tessera.transaction.exception.StoreEntityException;
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
