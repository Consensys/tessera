package com.quorum.tessera.jaxrs.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.shared.Constants;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import java.net.URI;
import org.junit.Test;

public class NodeUriHeaderDecoratorTest {

  @Test
  public void filter() throws Exception {

    ClientRequestContext requestContext = mock(ClientRequestContext.class);

    MultivaluedMap headers = new MultivaluedHashMap();
    when(requestContext.getHeaders()).thenReturn(headers);

    String serverUri = "http://bogus.com";

    ServerConfig serverConfig = mock(ServerConfig.class);
    when(serverConfig.isUnixSocket()).thenReturn(false);
    when(serverConfig.getServerUri()).thenReturn(URI.create(serverUri));
    NodeUriHeaderDecorator versionHeaderDecorator = new NodeUriHeaderDecorator(serverConfig);
    versionHeaderDecorator.filter(requestContext);

    assertThat(headers.getFirst(Constants.NODE_URI_HEADER)).isNotNull().isEqualTo(serverUri);

    verify(requestContext).getHeaders();
    verifyNoMoreInteractions(requestContext);
  }

  @Test
  public void filterNullServerUri() throws Exception {

    ClientRequestContext requestContext = mock(ClientRequestContext.class);

    MultivaluedMap headers = new MultivaluedHashMap();
    when(requestContext.getHeaders()).thenReturn(headers);

    ServerConfig serverConfig = mock(ServerConfig.class);
    when(serverConfig.isUnixSocket()).thenReturn(false);
    when(serverConfig.getServerUri()).thenReturn(null);
    NodeUriHeaderDecorator versionHeaderDecorator = new NodeUriHeaderDecorator(serverConfig);
    versionHeaderDecorator.filter(requestContext);

    assertThat(headers.getFirst(Constants.NODE_URI_HEADER))
        .isEqualTo(NodeUriHeaderDecorator.UNKNOWN);

    verify(requestContext).getHeaders();
    verifyNoMoreInteractions(requestContext);
  }

  @Test
  public void filterIpcFile() throws Exception {

    ClientRequestContext requestContext = mock(ClientRequestContext.class);

    MultivaluedMap headers = new MultivaluedHashMap();
    when(requestContext.getHeaders()).thenReturn(headers);

    String serverAddress = "/tmp/bogus.ipc";

    ServerConfig serverConfig = mock(ServerConfig.class);
    when(serverConfig.isUnixSocket()).thenReturn(true);
    when(serverConfig.getServerAddress()).thenReturn(serverAddress);

    NodeUriHeaderDecorator versionHeaderDecorator = new NodeUriHeaderDecorator(serverConfig);
    versionHeaderDecorator.filter(requestContext);

    assertThat(headers.getFirst(Constants.NODE_URI_HEADER)).isNotNull().isEqualTo(serverAddress);

    verify(requestContext).getHeaders();
    verifyNoMoreInteractions(requestContext);
  }
}
