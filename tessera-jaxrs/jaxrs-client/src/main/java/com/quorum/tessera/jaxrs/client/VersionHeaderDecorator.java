package com.quorum.tessera.jaxrs.client;

import com.quorum.tessera.shared.Constants;
import com.quorum.tessera.version.ApiVersion;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import java.io.IOException;

public class VersionHeaderDecorator implements ClientRequestFilter {

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        requestContext.getHeaders().addAll(Constants.API_VERSION_HEADER,ApiVersion.versions());

    }
}
