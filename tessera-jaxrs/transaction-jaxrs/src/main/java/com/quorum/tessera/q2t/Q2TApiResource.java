package com.quorum.tessera.q2t;

import com.quorum.tessera.api.common.AbstractApiResource;

import java.net.URL;

public class Q2TApiResource extends AbstractApiResource {

    private static final String RESOURCE_NAME = "openapi.q2t";

    @Override
    public String getResourceName() {
        return RESOURCE_NAME;
    }

    @Override
    public URL getOpenApiDocumentUrl(String name) {
        return getClass().getResource(name);
    }

}
