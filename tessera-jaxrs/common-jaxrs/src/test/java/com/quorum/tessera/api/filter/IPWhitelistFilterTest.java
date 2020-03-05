package com.quorum.tessera.api.filter;

import com.jpmorgan.quorum.mock.servicelocator.MockServiceLocator;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.context.RuntimeContextFactory;
import com.quorum.tessera.service.locator.ServiceLocator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class IPWhitelistFilterTest {

    private ContainerRequestContext ctx;

    private IPWhitelistFilter filter;

    private static RuntimeContext configService = RuntimeContextFactory.newFactory().create(mock(Config.class));

    @Before
    public void init() throws URISyntaxException {

        when(configService.getPeers()).thenReturn(singletonList(URI.create("http://whitelistedHost:8080")));
        when(configService.isUseWhiteList()).thenReturn(true);

        this.ctx = mock(ContainerRequestContext.class);
        final UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getBaseUri()).thenReturn(new URI("otherhost"));
        when(ctx.getUriInfo()).thenReturn(uriInfo);



        this.filter = new IPWhitelistFilter();
    }

    @After
    public void onTearDown() {
        reset(configService);
    }

    @Test
    public void disabledFilterAllowsAllRequests() {
        when(configService.getPeers()).thenReturn(emptyList());
        when(configService.isUseWhiteList()).thenReturn(false);
        this.filter = new IPWhitelistFilter();
        final HttpServletRequest request = mock(HttpServletRequest.class);
        doReturn("someotherhost").when(request).getRemoteAddr();
        doReturn("someotherhost").when(request).getRemoteHost();

        filter.setHttpServletRequest(request);
        filter.filter(ctx);

        verifyZeroInteractions(request);
        verifyZeroInteractions(ctx);
    }

    @Test
    public void hostNotInWhitelistGetsRejected() {

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
    public void hostInWhitelistGetsAccepted() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        doReturn("whitelistedHost").when(request).getRemoteAddr();

        filter.setHttpServletRequest(request);

        filter.filter(ctx);

        verify(request).getRemoteHost();
        verify(request).getRemoteAddr();
        verifyNoMoreInteractions(ctx);
    }

    @Test
    public void errorFilteringStopsFutureFilters() {


        when(configService.isUseWhiteList()).thenReturn(true);
        // show that one request goes through okay
        final HttpServletRequest request = mock(HttpServletRequest.class);
        doReturn("whitelistedHost").when(request).getRemoteAddr();
        filter.setHttpServletRequest(request);
        filter.filter(ctx);
        verify(request).getRemoteHost();
        verify(request).getRemoteAddr();

        verifyNoMoreInteractions(ctx);

        // show the second one errors
        final HttpServletRequest requestError = mock(HttpServletRequest.class);
        doThrow(RuntimeException.class).when(requestError).getRemoteHost();
        filter.setHttpServletRequest(requestError);
        filter.filter(ctx);

        verifyNoMoreInteractions(ctx);
        verify(request).getRemoteAddr();

        // show the third doesn't get filtered
        final HttpServletRequest requestIgnored = mock(HttpServletRequest.class);
        filter.setHttpServletRequest(requestIgnored);
        filter.filter(ctx);
        verifyZeroInteractions(requestIgnored);
        verifyZeroInteractions(ctx);
    }

    @Test
    public void defaultConstructor() {
        when(configService.isUseWhiteList()).thenReturn(Boolean.TRUE);
        MockServiceLocator mockServiceLocator = (MockServiceLocator) ServiceLocator.create();
        mockServiceLocator.setServices(Collections.singleton(configService));

        assertThat(new IPWhitelistFilter()).isNotNull();
    }

    @Test
    public void localhostIsWhiteListed() {

        URI peer = URI.create("http://localhost:8080");
        when(configService.getPeers()).thenReturn(singletonList(peer));

        final HttpServletRequest request = mock(HttpServletRequest.class);
        doReturn("127.0.0.1").when(request).getRemoteAddr();

        filter.setHttpServletRequest(request);

        filter.filter(ctx);

        verify(request).getRemoteHost();
        verify(request).getRemoteAddr();
        verifyNoMoreInteractions(ctx);
    }

    @Test
    public void localhostIPv6IsAlsoWhiteListed() {
        URI peer = URI.create("http://localhost:8080");
        when(configService.getPeers()).thenReturn(singletonList(peer));

        final HttpServletRequest request = mock(HttpServletRequest.class);
        doReturn("0:0:0:0:0:0:0:1").when(request).getRemoteAddr();

        filter.setHttpServletRequest(request);

        filter.filter(ctx);

        verify(request).getRemoteHost();
        verify(request).getRemoteAddr();
        verifyNoMoreInteractions(ctx);
    }
}
