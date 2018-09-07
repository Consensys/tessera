package com.quorum.tessera.api.filter;

import com.quorum.tessera.ssl.util.HostnameUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import java.net.UnknownHostException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class PrivateApiFilterTest {

    private ContainerRequestContext ctx;

    private PrivateApiFilter filter;

    @Before
    public void init() throws UnknownHostException {

        this.filter = new PrivateApiFilter();

        this.ctx = mock(ContainerRequestContext.class);
    }

    @Test
    public void hostNotALocalAddressGetsRejected() {

        final Response expectedResponse = Response.status(Response.Status.UNAUTHORIZED).build();

        final HttpServletRequest request = mock(HttpServletRequest.class);
        doReturn("127.0.0.2").when(request).getRemoteAddr();

        filter.setHttpServletRequest(request);

        filter.filter(ctx);

        verify(request).getRemoteAddr();

        final ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(ctx).abortWith(captor.capture());

        assertThat(captor.getValue()).isEqualToComparingFieldByFieldRecursively(expectedResponse);

    }

    @Test
    public void hostThatIsLocalAddressGetsAccepted() throws UnknownHostException {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        doReturn(HostnameUtil.create().getHostIpAddress()).when(request).getRemoteAddr();

        filter.setHttpServletRequest(request);

        filter.filter(ctx);

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
