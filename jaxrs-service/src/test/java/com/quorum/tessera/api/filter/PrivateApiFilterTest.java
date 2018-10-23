package com.quorum.tessera.api.filter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class PrivateApiFilterTest {

    private ContainerRequestContext ctx;

    private PrivateApiFilter filter = new PrivateApiFilter();

    @Before
    public void init() {
        this.ctx = mock(ContainerRequestContext.class);
    }

    @Test
    public void hostWithWrongBaseUriFails() throws URISyntaxException {

        final Response expectedResponse = Response.status(Response.Status.UNAUTHORIZED).build();

        final UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getBaseUri()).thenReturn(new URI("otherhost.com:8080"));
        when(ctx.getUriInfo()).thenReturn(uriInfo);

        filter.filter(ctx);

        final ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(ctx).abortWith(captor.capture());

        assertThat(captor.getValue()).isEqualToComparingFieldByFieldRecursively(expectedResponse);

    }

    @Test
    public void hostWithCorrectBaseUriSucceeds() throws URISyntaxException {

        final UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getBaseUri()).thenReturn(new URI("unixsocket"));
        when(ctx.getUriInfo()).thenReturn(uriInfo);

        filter.filter(ctx);

        verify(ctx).getUriInfo();
        verifyNoMoreInteractions(ctx);

    }

}
