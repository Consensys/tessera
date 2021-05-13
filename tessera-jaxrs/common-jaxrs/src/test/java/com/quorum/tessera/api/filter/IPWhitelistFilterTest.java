package com.quorum.tessera.api.filter;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.jpmorgan.quorum.mock.servicelocator.MockServiceLocator;
import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.mock.MockRuntimeContextFactory;
import com.quorum.tessera.service.locator.ServiceLocator;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class IPWhitelistFilterTest {

  private ContainerRequestContext ctx;

  private IPWhitelistFilter filter;

  private RuntimeContext runtimeContext;

  @Before
  public void init() throws URISyntaxException {
    runtimeContext = mock(RuntimeContext.class);
    MockRuntimeContextFactory.setMockContext(runtimeContext);

    when(runtimeContext.getPeers())
        .thenReturn(singletonList(URI.create("http://whitelistedHost:8080")));
    when(runtimeContext.isUseWhiteList()).thenReturn(true);

    this.ctx = mock(ContainerRequestContext.class);
    final UriInfo uriInfo = mock(UriInfo.class);
    when(uriInfo.getBaseUri()).thenReturn(new URI("otherhost"));
    when(ctx.getUriInfo()).thenReturn(uriInfo);

    this.filter = new IPWhitelistFilter();
  }

  @After
  public void onTearDown() {
    reset(runtimeContext);
    MockRuntimeContextFactory.reset();
  }

  @Test
  public void disabledFilterAllowsAllRequests() {
    when(runtimeContext.getPeers()).thenReturn(emptyList());
    when(runtimeContext.isUseWhiteList()).thenReturn(false);
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
  public void defaultConstructor() {
    when(runtimeContext.isUseWhiteList()).thenReturn(Boolean.TRUE);
    MockServiceLocator mockServiceLocator = (MockServiceLocator) ServiceLocator.create();
    mockServiceLocator.setServices(Collections.singleton(runtimeContext));

    assertThat(new IPWhitelistFilter()).isNotNull();
  }

  @Test
  public void localhostIsWhiteListed() {

    URI peer = URI.create("http://localhost:8080");
    when(runtimeContext.getPeers()).thenReturn(singletonList(peer));

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
    when(runtimeContext.getPeers()).thenReturn(singletonList(peer));

    final HttpServletRequest request = mock(HttpServletRequest.class);
    doReturn("0:0:0:0:0:0:0:1").when(request).getRemoteAddr();

    filter.setHttpServletRequest(request);

    filter.filter(ctx);

    verify(request).getRemoteHost();
    verify(request).getRemoteAddr();
    verifyNoMoreInteractions(ctx);
  }

  @Test
  public void localAddrIPv6IsAlsoWhiteListed() {
    URI peer = URI.create("http://127.0.0.1:8080");
    when(runtimeContext.getPeers()).thenReturn(singletonList(peer));

    final HttpServletRequest request = mock(HttpServletRequest.class);
    doReturn("0:0:0:0:0:0:0:1").when(request).getRemoteAddr();

    filter.setHttpServletRequest(request);

    filter.filter(ctx);

    verify(request).getRemoteHost();
    verify(request).getRemoteAddr();
    verifyNoMoreInteractions(ctx);
  }
}
