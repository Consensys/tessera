package com.quorum.tessera.api.filter;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.Peer;
import com.quorum.tessera.config.ServerConfig;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

public class IPWhitelistFilterTest {

    private ContainerRequestContext ctx;

    private IPWhitelistFilter filter;

    @Before
    public void init() throws URISyntaxException, UnknownHostException {

        this.ctx = mock(ContainerRequestContext.class);

        final Config configuration = mock(Config.class);

        Peer peer = new Peer("http://whitelistedHost:8080");
        when(configuration.getPeers()).thenReturn(Collections.singletonList(peer));
        when(configuration.isUseWhiteList()).thenReturn(true);

        final ServerConfig serverConfig = mock(ServerConfig.class);
        when(serverConfig.getBindingAddress()).thenReturn("http://localhost:8080");
        when(configuration.getServerConfig()).thenReturn(serverConfig);

        this.filter = new IPWhitelistFilter(configuration);

    }

    @Test
    public void disabledFilterAllowsAllRequests() throws URISyntaxException, UnknownHostException {
        final Config configuration = mock(Config.class);
        when(configuration.getPeers()).thenReturn(Collections.emptyList());
        when(configuration.isUseWhiteList()).thenReturn(false);

        final ServerConfig serverConfig = mock(ServerConfig.class);
        when(serverConfig.getBindingAddress()).thenReturn("http://localhost:8080");
        when(configuration.getServerConfig()).thenReturn(serverConfig);

        final IPWhitelistFilter filter = new IPWhitelistFilter(configuration);

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

    @Test
    public void selfIsWhitelisted() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        doReturn("127.0.0.1").when(request).getRemoteAddr();

        filter.setHttpServletRequest(request);

        filter.filter(ctx);

        verify(request).getRemoteHost();
        verify(request).getRemoteAddr();
        verifyZeroInteractions(ctx);
    }

    @Test
    public void invalidPeerCantBeWhitelisted() throws URISyntaxException {
        final Config configuration = mock(Config.class);

        Peer peer = new Peer("ht:whitelistedHost:8080");
        when(configuration.getPeers()).thenReturn(Collections.singletonList(peer));
        when(configuration.isUseWhiteList()).thenReturn(true);

        final ServerConfig serverConfig = mock(ServerConfig.class);
        when(serverConfig.getServerUri()).thenReturn(new URI("http://localhost:8080"));
        when(configuration.getServerConfig()).thenReturn(serverConfig);

        final Throwable throwable = catchThrowable(() -> new IPWhitelistFilter(configuration));

        assertThat(throwable)
            .isInstanceOf(RuntimeException.class)
            .hasCauseExactlyInstanceOf(MalformedURLException.class);

        assertThat(throwable.getCause()).hasMessage("unknown protocol: ht");
    }

}
