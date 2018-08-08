package com.quorum.tessera.api.filter;

import com.quorum.tessera.config.ServerConfig;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;

import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class PrivateApiFilterTest {

    private ContainerRequestContext ctx;

    private PrivateApiFilter filter;

    @Before
    public void init() throws URISyntaxException {
        final ServerConfig serverConfig = mock(ServerConfig.class);
        final URI testUri = new URI("http://localhost:8080");
        when(serverConfig.getServerUri()).thenReturn(testUri);

        this.filter = new PrivateApiFilter(serverConfig);

        this.ctx = mock(ContainerRequestContext.class);
    }

    @Test
    public void hostNotALocalAddressGetsRejected() {

        final Response expectedResponse = Response.status(Response.Status.UNAUTHORIZED).build();

        final HttpServletRequest request = mock(HttpServletRequest.class);
        doReturn("someotherhost").when(request).getRemoteAddr();
        doReturn("someotherhost").when(request).getRemoteHost();

        filter.setHttpServletRequest(request);

        filter.filter(ctx);

        verify(request).getRemoteHost();
        verify(request).getRemoteAddr();

        final ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(ctx).abortWith(captor.capture());

        assertThat(captor.getValue()).isEqualToComparingFieldByFieldRecursively(expectedResponse);

    }

    @Test
    public void hostThatIsLocalAddressGetsAccepted() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        doReturn("localhost").when(request).getRemoteAddr();

        filter.setHttpServletRequest(request);

        filter.filter(ctx);

        verify(request).getRemoteHost();
        verify(request).getRemoteAddr();
        verifyZeroInteractions(ctx);

    }

    @Test
    public void hostThatIsLocalHostnameGetsAccepted() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        doReturn("localhost").when(request).getRemoteHost();
        doReturn("wrongvalue").when(request).getRemoteAddr();

        filter.setHttpServletRequest(request);

        filter.filter(ctx);

        verify(request).getRemoteHost();
        verify(request).getRemoteAddr();
        verifyZeroInteractions(ctx);

    }

    @Test
    public void noServletAllowsRequest() {

        filter.setHttpServletRequest(null);

        filter.filter(ctx);

        verifyZeroInteractions(ctx);

    }

}
