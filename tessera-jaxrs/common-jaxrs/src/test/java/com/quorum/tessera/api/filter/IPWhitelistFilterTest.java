package com.quorum.tessera.api.filter;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.context.RuntimeContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

public class IPWhitelistFilterTest {

  private ContainerRequestContext ctx;

  private IPWhitelistFilter filter;

  private RuntimeContext runtimeContext;

  private MockedStatic<RuntimeContext> runtimeContextMockedStatic;

  @Before
  public void init() throws URISyntaxException {

    runtimeContext = mock(RuntimeContext.class);
    runtimeContextMockedStatic = mockStatic(RuntimeContext.class);
    runtimeContextMockedStatic.when(RuntimeContext::getInstance).thenReturn(runtimeContext);

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
    try {
      verifyNoMoreInteractions(runtimeContext);
    } finally {
      runtimeContextMockedStatic.close();
    }
  }

  @Test
  public void disabledFilterAllowsAllRequests() {

    when(runtimeContext.getPeers()).thenReturn(emptyList());
    when(runtimeContext.isUseWhiteList()).thenReturn(false);

    final HttpServletRequest request = mock(HttpServletRequest.class);
    doReturn("someotherhost").when(request).getRemoteAddr();
    doReturn("someotherhost").when(request).getRemoteHost();

    filter.setHttpServletRequest(request);
    filter.filter(ctx);

    verifyNoInteractions(request);
    verifyNoInteractions(ctx);

    verify(runtimeContext).isUseWhiteList();
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
    verify(runtimeContext).isUseWhiteList();
    verify(runtimeContext).getPeers();
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

    verify(runtimeContext).isUseWhiteList();
    verify(runtimeContext).getPeers();
  }

  @Test
  public void defaultConstructor() {
    when(runtimeContext.isUseWhiteList()).thenReturn(Boolean.TRUE);

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

    verify(runtimeContext).isUseWhiteList();
    verify(runtimeContext).getPeers();
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

    verify(runtimeContext).isUseWhiteList();
    verify(runtimeContext).getPeers();
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

    verify(runtimeContext).isUseWhiteList();
    verify(runtimeContext).getPeers();
  }
}
