package com.github.nexus.api.exception;

import com.github.nexus.api.exception.KeyNotFoundExceptionMapper;
import com.github.nexus.key.exception.KeyNotFoundException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyNotFoundExceptionMapperTest {

    private KeyNotFoundExceptionMapper instance;

    public KeyNotFoundExceptionMapperTest() {
    }



    @Before
    public void setUp() {
        instance = new KeyNotFoundExceptionMapper();
    }

    @After
    public void tearDown() {
    }


    @Test
    public void toResponse() {

        KeyNotFoundException keyNotFoundException = new KeyNotFoundException("OUCH");

        Response result = instance.toResponse(keyNotFoundException);
        assertThat(result).isNotNull();

        String message = (String) result.getEntity();

        assertThat(message).isEqualTo("OUCH");

        assertThat(result.getStatus()).isEqualTo(400);

    }
}
