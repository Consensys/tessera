package com.github.tessera.transaction.exception;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TransactionNotFoundExceptionTest {
    public TransactionNotFoundExceptionTest() {
    }

    @Test
    public void constructWithMessage() {

        String message = "Some punk's busted up my ride!!";

        TransactionNotFoundException transactionNotFoundException = new TransactionNotFoundException(message);

        assertThat(transactionNotFoundException.getMessage()).isEqualTo(message);


    }

    @Test
    public void constructWithMessageAndCause() {

        String message = "Some punk's busted up my ride!!";
        Throwable cause = new Exception("OUCH");
        TransactionNotFoundException transactionNotFoundException = new TransactionNotFoundException(message,cause);

        assertThat(transactionNotFoundException.getMessage()).isEqualTo(message);
        assertThat(transactionNotFoundException.getCause()).isSameAs(cause);
    }


    @Test
    public void constructWithCause() {

        Throwable cause = new Exception("OUCH");
        TransactionNotFoundException transactionNotFoundException = new TransactionNotFoundException(cause);

        assertThat(transactionNotFoundException.getMessage()).isEqualTo("java.lang.Exception: OUCH");
        assertThat(transactionNotFoundException.getCause()).isSameAs(cause);

    }
}
