package com.quorum.tessera.api.exception;

import com.quorum.tessera.transaction.exception.TransactionNotFoundException;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class TransactionNotFoundExceptionMapperTest {

    private TransactionNotFoundExceptionMapper instance;

    @Before
    public void setUp() {
        instance = new TransactionNotFoundExceptionMapper();
    }

    @Test
    public void toResponse() {

        final TransactionNotFoundException transactionNotFoundException = new TransactionNotFoundException("OUCH");

        final Response result = instance.toResponse(transactionNotFoundException);
        assertThat(result).isNotNull();

        final String message = result.getEntity().toString();

        assertThat(message).isEqualTo("OUCH");

        assertThat(result.getStatus()).isEqualTo(404);

    }
}
