package com.quorum.tessera.api.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.transaction.publish.NodeOfflineException;
import java.net.URI;
import javax.ws.rs.core.Response;
import org.junit.Before;
import org.junit.Test;

public class NodeOfflineExceptionMapperTest {

  private Discovery discovery;

  private NodeOfflineExceptionMapper exceptionMapper;

  @Before
  public void beforeTest() {
    discovery = mock(Discovery.class);
    exceptionMapper = new NodeOfflineExceptionMapper(discovery);
  }

  @Test
  public void toResponse() {
    URI uri = URI.create("http://ouchthatsgottasmart.com");
    final NodeOfflineException exception = new NodeOfflineException(uri);

    final Response result = exceptionMapper.toResponse(exception);

    verify(discovery).onDisconnect(uri);

    assertThat(result.getStatus()).isEqualTo(Response.Status.GONE.getStatusCode());
    assertThat(result.getStatusInfo().getReasonPhrase())
        .isEqualTo("Connection error while communicating with http://ouchthatsgottasmart.com");
  }

  @Test
  public void defaultConstructor() {
    assertThat(new NodeOfflineExceptionMapper()).isNotNull();
  }
}
