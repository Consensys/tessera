package com.quorum.tessera.jaxrs.client;

import com.quorum.tessera.shared.Constants;
import com.quorum.tessera.version.ApiVersion;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import java.io.IOException;

public class VersionHeaderDecorator implements ClientResponseFilter {

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        responseContext.getHeaders().addAll(Constants.API_VERSION_HEADER,ApiVersion.versions());

    }
}
