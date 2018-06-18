package com.github.nexus.api.exception;

import com.github.nexus.transaction.exception.TransactionNotFoundException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class TransactionNotFoundExceptionMapperTest {
    private TransactionNotFoundExceptionMapper instance;

    public TransactionNotFoundExceptionMapperTest() {
    }



    @Before
    public void setUp() {
        instance = new TransactionNotFoundExceptionMapper();
    }

    @After
    public void tearDown() {
    }


    @Test
    public void toResponse() {

        TransactionNotFoundException transactionNotFoundException = new TransactionNotFoundException("OUCH");

        Response result = instance.toResponse(transactionNotFoundException);
        assertThat(result).isNotNull();

        String message = (String) result.getEntity();

        assertThat(message).isEqualTo("OUCH");

        assertThat(result.getStatus()).isEqualTo(400);

    }
}
