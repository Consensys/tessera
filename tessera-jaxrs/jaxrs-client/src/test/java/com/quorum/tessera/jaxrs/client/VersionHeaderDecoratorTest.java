package com.quorum.tessera.jaxrs.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.shared.Constants;
import com.quorum.tessera.version.ApiVersion;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.junit.Test;

public class VersionHeaderDecoratorTest {

  @Test
  public void filter() throws Exception {

    ClientRequestContext requestContext = mock(ClientRequestContext.class);

    MultivaluedMap headers = new MultivaluedHashMap();
    when(requestContext.getHeaders()).thenReturn(headers);

    VersionHeaderDecorator versionHeaderDecorator = new VersionHeaderDecorator();
    versionHeaderDecorator.filter(requestContext);

    assertThat(headers.get(Constants.API_VERSION_HEADER))
        .isNotNull()
        .isEqualTo(ApiVersion.versions());

    int count = ApiVersion.versions().size();

    verify(requestContext, times(count)).getHeaders();
    verifyNoMoreInteractions(requestContext);
  }
}
