package com.quorum.tessera.api;

import com.quorum.tessera.api.filter.LoggingFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class LoggingFilterTest {

    private LoggingFilter loggingFilter;

    public LoggingFilterTest() {
    }

    @Before
    public void setUp() {
        loggingFilter = new LoggingFilter();
    }

    @After
    public void tearDown() {
        loggingFilter = null;
    }

    @Test
    public void filterRequest() {
        ContainerRequestContext request = mock(ContainerRequestContext.class);
        loggingFilter.filter(request);
        //Very silly test 
        assertThat(loggingFilter).isNotNull();

    }

    @Test
    public void filterRequestAndResponse() {
        ContainerRequestContext request = mock(ContainerRequestContext.class);
        ContainerResponseContext response = mock(ContainerResponseContext.class);
        loggingFilter.filter(request,response);
        //Very silly test 
        assertThat(loggingFilter).isNotNull();

    }
}
