package com.github.tessera.transaction.exception;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TransactionNotFoundExceptionTest {

    @Test
    public void constructWithMessage() {

        final String message = "Some punk's busted up my ride!!";

        final TransactionNotFoundException testException = new TransactionNotFoundException(message);

        assertThat(testException.getMessage()).isEqualTo(message);

    }

}
