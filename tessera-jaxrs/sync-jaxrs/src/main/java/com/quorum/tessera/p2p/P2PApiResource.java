package com.quorum.tessera.p2p;

import com.quorum.tessera.api.common.AbstractApiResource;

import java.net.URL;

public class P2PApiResource extends AbstractApiResource {

    private static final String RESOURCE_NAME = "openapi.p2p";

    @Override
    public String getResourceName() {
        return RESOURCE_NAME;
    }

    @Override
    public URL getOpenApiDocumentUrl(String name) {
        return getClass().getResource(name);
    }


}
