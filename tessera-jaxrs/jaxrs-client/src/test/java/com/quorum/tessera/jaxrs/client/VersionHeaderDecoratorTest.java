package com.quorum.tessera.jaxrs.client;

import com.quorum.tessera.shared.Constants;
import com.quorum.tessera.version.ApiVersion;
import org.junit.Test;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class VersionHeaderDecoratorTest {

    @Test
    public void filter() throws Exception {

        ClientRequestContext requestContext = mock(ClientRequestContext.class);

        MultivaluedMap headers = new MultivaluedHashMap();
        when(requestContext.getHeaders()).thenReturn(headers);

        VersionHeaderDecorator versionHeaderDecorator = new VersionHeaderDecorator();
        versionHeaderDecorator.filter(requestContext);

        assertThat(headers.get(Constants.API_VERSION_HEADER)).isNotNull().isEqualTo(ApiVersion.versions());

        verify(requestContext).getHeaders();
        verifyNoMoreInteractions(requestContext);
    }
}
