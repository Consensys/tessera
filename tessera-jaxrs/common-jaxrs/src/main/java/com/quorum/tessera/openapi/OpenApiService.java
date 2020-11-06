package com.quorum.tessera.openapi;

import io.swagger.v3.jaxrs2.integration.resources.BaseOpenApiResource;

import javax.servlet.ServletConfig;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

// Simple concrete implementation of BaseOpenApiResource abstract class; allows for mocking of getOpenApi in testing
public class OpenApiService extends BaseOpenApiResource {

    @Override
    public Response getOpenApi(HttpHeaders headers, ServletConfig config, Application app, UriInfo uriInfo, String type) throws Exception {
        return super.getOpenApi(headers, config, app, uriInfo, type);
    }
}
