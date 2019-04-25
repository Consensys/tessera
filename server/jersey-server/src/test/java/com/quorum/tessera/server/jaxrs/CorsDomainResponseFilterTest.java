package com.quorum.tessera.server.jaxrs;

import java.net.URI;
import java.util.Collections;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class CorsDomainResponseFilterTest {

    private CorsDomainResponseFilter domainResponseFilter;

    private ContainerRequestContext requestContext;

    private ContainerResponseContext responseContext;

    private UriInfo uriInfo;

    @Before
    public void setUp() {
        domainResponseFilter = new CorsDomainResponseFilter(Collections.EMPTY_LIST);
        requestContext = mock(ContainerRequestContext.class);
        responseContext = mock(ContainerResponseContext.class);
        uriInfo = mock(UriInfo.class);

        when(requestContext.getUriInfo()).thenReturn(uriInfo);
    }

    @After
    public void tearDown() {
        domainResponseFilter = null;
        verifyNoMoreInteractions(requestContext, responseContext);
    }

    @Test
    public void ignoreUnixSocket() throws Exception {

        when(uriInfo.getBaseUri()).thenReturn(new URI("unixsocket"));

        when(requestContext.getUriInfo()).thenReturn(uriInfo);

        domainResponseFilter.filter(requestContext, responseContext);

        verify(requestContext).getUriInfo();

    }

    @Test
    public void filter() throws Exception {

        MultivaluedMap<String, Object> headers = new MultivaluedHashMap();

        when(responseContext.getHeaders()).thenReturn(headers);

        when(uriInfo.getBaseUri()).thenReturn(new URI("http://bogus.com/"));

        when(requestContext.getUriInfo()).thenReturn(uriInfo);

        when(requestContext.getHeaderString("Origin")).thenReturn("bogus.com");

        when(requestContext.getHeaderString("Access-Control-Request-Headers"))
            .thenReturn("SomeHeaders");

        domainResponseFilter.filter(requestContext, responseContext);

        assertThat(headers)
            .containsKeys(
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials", 
                "Access-Control-Request-Headers");

        assertThat(headers.get("Access-Control-Allow-Origin")).containsExactly("bogus.com");
        assertThat(headers.get("Access-Control-Allow-Credentials")).containsExactly("true");
        assertThat(headers.get("Access-Control-Request-Headers")).containsExactly("SomeHeaders");

        verify(requestContext).getUriInfo();
        verify(requestContext).getHeaderString("Origin");
        verify(requestContext).getHeaderString("Access-Control-Request-Headers");
        verify(responseContext).getHeaders();
    }

    @Test
    public void ignoreNoOrigin() throws Exception {

        when(uriInfo.getBaseUri()).thenReturn(new URI("bogus.com"));

        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(requestContext.getHeaderString("Origin")).thenReturn(null);

        domainResponseFilter.filter(requestContext, responseContext);

        verify(requestContext).getUriInfo();
        verify(requestContext).getHeaderString("Origin");

    }
    
    @Test
    public void ignoreEmptyOrigin() throws Exception {

        when(uriInfo.getBaseUri()).thenReturn(new URI("bogus.com"));

        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(requestContext.getHeaderString("Origin")).thenReturn("");

        domainResponseFilter.filter(requestContext, responseContext);

        verify(requestContext).getUriInfo();
        verify(requestContext).getHeaderString("Origin");

    }
}
