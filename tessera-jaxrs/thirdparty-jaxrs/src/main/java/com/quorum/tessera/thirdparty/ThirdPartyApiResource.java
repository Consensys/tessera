package com.quorum.tessera.thirdparty;

import com.quorum.tessera.api.common.AbstractApiResource;

import java.net.URL;

public class ThirdPartyApiResource extends AbstractApiResource {

    private static final String RESOURCE_NAME = "openapi.thirdparty";

    @Override
    public String getResourceName() {
        return RESOURCE_NAME;
    }

    @Override
    public URL getOpenApiDocumentUrl(String name) {
        return getClass().getResource(name);
    }

}
