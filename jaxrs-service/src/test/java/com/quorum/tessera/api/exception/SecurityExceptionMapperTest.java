package com.quorum.tessera.api.exception;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class SecurityExceptionMapperTest {

    private SecurityExceptionMapper instance;

    public SecurityExceptionMapperTest() {
    }

    @Before
    public void setUp() {
        instance = new SecurityExceptionMapper();
    }

    @After
    public void tearDown() {
    }


    @Test
    public void toResponse() {

        SecurityException securityException = new SecurityException("OUCH");

        Response result = instance.toResponse(securityException);
        assertThat(result).isNotNull();
        assertThat(result.getEntity()).isNull();

        assertThat(result.getStatus()).isEqualTo(500);

    }
}
