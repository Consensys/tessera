package com.quorum.tessera.api.filter;

import com.jpmorgan.quorum.mock.servicelocator.MockServiceLocator;
import com.quorum.tessera.admin.ConfigService;
import com.quorum.tessera.config.Peer;
import com.quorum.tessera.service.locator.ServiceLocator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class IPWhitelistFilterTest {

    private ContainerRequestContext containerRequestContext;

    private ConfigService configService;

    @Before
    public void onSetUp()  {

        this.containerRequestContext = mock(ContainerRequestContext.class);
        final UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getBaseUri()).thenReturn( URI.create("otherhost"));
        when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);

        this.configService = mock(ConfigService.class);

        Peer peer = new Peer("http://whitelistedHost:8080");
        when(configService.getPeers()).thenReturn(singletonList(peer));
        when(configService.isDisablePeerDiscovery()).thenReturn(false);

    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(containerRequestContext);
        verifyNoMoreInteractions(configService);
    }

    @Test
    public void autoDiscoveryEnabledAndHostInPeersList() {

        String validHostName = "someotherhost";

        when(configService.isDisablePeerDiscovery()).thenReturn(true);

        Peer peer = mock(Peer.class);
        when(peer.getUrl()).thenReturn(String.format("http://%s:8080",validHostName));

        when(configService.getPeers()).thenReturn(Arrays.asList(peer));
        IPWhitelistFilter filter = new IPWhitelistFilter(configService);

        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn(validHostName);
        when(request.getRemoteHost()).thenReturn(validHostName);

        filter.setHttpServletRequest(request);
        filter.filter(containerRequestContext);

        verify(request).getRemoteAddr();
        verify(request).getRemoteHost();
        verify(configService).isDisablePeerDiscovery();
        verify(configService).getPeers();
        verify(containerRequestContext).getUriInfo();

        verifyNoMoreInteractions(request);
    }

    @Test
    public void autoDiscoveryEnabledAndHostNotInPeersList() {

        when(configService.isDisablePeerDiscovery()).thenReturn(true);
        when(configService.getPeers()).thenReturn(EMPTY_LIST);


        IPWhitelistFilter filter = new IPWhitelistFilter(configService);

        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("someotherhost");
        when(request.getRemoteHost()).thenReturn("someotherhost");

        filter.setHttpServletRequest(request);

        List<Response> responses = new ArrayList<>();
        doAnswer(invocation -> {
            responses.add(invocation.getArgument(0));
            return null;
        }).when(containerRequestContext).abortWith(any(Response.class));

        filter.filter(containerRequestContext);

        verify(request).getRemoteHost();
        verify(request).getRemoteAddr();

        verify(containerRequestContext)
            .abortWith(any(Response.class));

        assertThat(responses).hasSize(1);
        Response response = responses.get(0);
        assertThat(response.getStatus()).isEqualTo(401);

        verify(configService).isDisablePeerDiscovery();
        verify(configService).getPeers();

        verify(containerRequestContext).getUriInfo();

        verifyNoMoreInteractions(request);

    }



    @Test
    public void unixsocketRequestsIgnored() {

        when(configService.isDisablePeerDiscovery()).thenReturn(true);

        IPWhitelistFilter filter = new IPWhitelistFilter(configService);

        final HttpServletRequest requestError = mock(HttpServletRequest.class);
        filter.setHttpServletRequest(requestError);

        final UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getBaseUri()).thenReturn(URI.create("unixsocket"));
        when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);

        filter.filter(containerRequestContext);

        verify(configService).isDisablePeerDiscovery();
        verify(containerRequestContext).getUriInfo();
        verifyNoMoreInteractions(containerRequestContext);
        verifyZeroInteractions(requestError);
    }

    @Test
    public void defaultConstructor() {

        when(configService.isDisablePeerDiscovery()).thenReturn(false);

        MockServiceLocator mockServiceLocator = (MockServiceLocator) ServiceLocator.create();
        mockServiceLocator.setServices(Collections.singleton(configService));

        IPWhitelistFilter defaultInstance = new IPWhitelistFilter();
        assertThat(defaultInstance).isNotNull();
        verify(configService).isDisablePeerDiscovery();
    }

    @Test
    public void autoDisoveryEnabledDeactivatedFiltering() {

        when(configService.isDisablePeerDiscovery()).thenReturn(false);

        IPWhitelistFilter filter = new IPWhitelistFilter(configService);
        verify(configService).isDisablePeerDiscovery();

        filter.filter(containerRequestContext);

        verifyNoMoreInteractions(configService);
        verifyZeroInteractions(containerRequestContext);

    }
}
