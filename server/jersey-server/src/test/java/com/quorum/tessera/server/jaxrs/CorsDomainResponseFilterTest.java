package com.quorum.tessera.server.jaxrs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.quorum.tessera.config.CrossDomainConfig;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CorsDomainResponseFilterTest {

  private static final String SOME_ORIGIN = "http://bogus.com";

  private CorsDomainResponseFilter domainResponseFilter;

  private ContainerRequestContext requestContext;

  private ContainerResponseContext responseContext;

  @Before
  public void setUp() {

    CrossDomainConfig crossDomainConfig = new CrossDomainConfig();
    crossDomainConfig.setAllowedOrigins(Arrays.asList(SOME_ORIGIN));

    domainResponseFilter = new CorsDomainResponseFilter(crossDomainConfig);
    requestContext = mock(ContainerRequestContext.class);
    responseContext = mock(ContainerResponseContext.class);
  }

  @After
  public void tearDown() {
    domainResponseFilter = null;
    verifyNoMoreInteractions(requestContext, responseContext);
  }

  @Test
  public void filter() throws Exception {

    MultivaluedMap<String, Object> headers = new MultivaluedHashMap();

    when(responseContext.getHeaders()).thenReturn(headers);

    when(requestContext.getHeaderString("Origin")).thenReturn(SOME_ORIGIN);

    domainResponseFilter.filter(requestContext, responseContext);

    assertThat(headers)
        .containsKeys(
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials",
            "Access-Control-Allow-Methods",
            "Access-Control-Allow-Headers");

    assertThat(headers.get("Access-Control-Allow-Origin")).containsExactly(SOME_ORIGIN);
    assertThat(headers.get("Access-Control-Allow-Credentials")).containsExactly("true");

    verify(requestContext).getHeaderString("Origin");
    verify(requestContext).getHeaderString("Access-Control-Request-Headers");
    verify(responseContext).getHeaders();
  }

  @Test
  public void ignoreNoOrigin() throws Exception {

    when(requestContext.getHeaderString("Origin")).thenReturn(null);

    domainResponseFilter.filter(requestContext, responseContext);
    verify(requestContext).getHeaderString("Origin");
  }

  @Test
  public void ignoreEmptyOrigin() throws Exception {

    when(requestContext.getHeaderString("Origin")).thenReturn("");

    domainResponseFilter.filter(requestContext, responseContext);

    verify(requestContext).getHeaderString("Origin");
  }
}
