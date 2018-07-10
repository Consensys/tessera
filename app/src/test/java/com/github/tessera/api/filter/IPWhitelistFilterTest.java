package com.github.tessera.api.filter;

import com.github.tessera.config.Config;
import com.github.tessera.config.Peer;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.*;

public class IPWhitelistFilterTest {

    private ContainerRequestContext ctx;

    private IPWhitelistFilter filter;

    @Before
    public void init() {

        this.ctx = mock(ContainerRequestContext.class);

        final Config configuration = mock(Config.class);
        Peer peer = mock(Peer.class);
        when(peer.getUrl()).thenReturn("whitelistedHost");
        when(configuration.getPeers())
                .thenReturn(Arrays.asList(peer));
        when(configuration.isUseWhiteList()).thenReturn(true);
        this.filter = new IPWhitelistFilter(configuration);

    }

    @Test
    public void noAddressesInWhitelistDisablesFilter() {
          final Config configuration = mock(Config.class);
          when(configuration.getPeers()).thenReturn(Collections.EMPTY_LIST);
          when(configuration.isUseWhiteList()).thenReturn(false);
          
        final IPWhitelistFilter filter = new IPWhitelistFilter(configuration);

        final HttpServletRequest request = mock(HttpServletRequest.class);

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
        verifyZeroInteractions(ctx);

    }

    @Test
    public void errorFilteringStopsFutureFilters() {

        //show that one request goes through okay
        final HttpServletRequest request = mock(HttpServletRequest.class);
        doReturn("whitelistedHost").when(request).getRemoteAddr();
        filter.setHttpServletRequest(request);
        filter.filter(ctx);
        verify(request).getRemoteHost();
        verify(request).getRemoteAddr();
        verifyZeroInteractions(ctx);

        //show the second one errors
        final HttpServletRequest requestError = mock(HttpServletRequest.class);
        doThrow(RuntimeException.class).when(requestError).getRemoteHost();
        filter.setHttpServletRequest(requestError);
        filter.filter(ctx);
        verifyZeroInteractions(ctx);
        verify(request).getRemoteAddr();

        //show the third doesn't get filtered
        final HttpServletRequest requestIgnored = mock(HttpServletRequest.class);
        filter.setHttpServletRequest(requestIgnored);
        filter.filter(ctx);
        verifyZeroInteractions(requestIgnored);
        verifyZeroInteractions(ctx);
    }



}
