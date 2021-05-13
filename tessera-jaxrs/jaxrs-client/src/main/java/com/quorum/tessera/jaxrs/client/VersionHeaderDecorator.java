package com.quorum.tessera.jaxrs.client;

import com.quorum.tessera.shared.Constants;
import com.quorum.tessera.version.ApiVersion;
import java.io.IOException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

public class VersionHeaderDecorator implements ClientRequestFilter {

  @Override
  public void filter(ClientRequestContext requestContext) throws IOException {
    ApiVersion.versions()
        .forEach(v -> requestContext.getHeaders().add(Constants.API_VERSION_HEADER, v));
  }
}
