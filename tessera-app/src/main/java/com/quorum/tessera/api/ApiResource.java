package com.quorum.tessera.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Variant;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import static javax.ws.rs.core.MediaType.*;

/**
 * Provides HTTP endpoints for accessing the OpenAPI schema
 */
@Path("/api")
@Api("Provides access to OpenAPI schema documentation.")
public class ApiResource {

    private static final List<Variant> VARIANTS = Variant.mediaTypes(APPLICATION_JSON_TYPE, TEXT_HTML_TYPE).build();

    @GET
    @Produces({APPLICATION_JSON, TEXT_HTML})
    @ApiResponses({@ApiResponse(code = 200, message = "Returns JSON or HTML OpenAPI document")})
    public Response api(@Context final Request request) throws IOException {

        final Variant variant = request.selectVariant(VARIANTS);

        final URL url;
        if (variant.getMediaType() == APPLICATION_JSON_TYPE) {
            url = getClass().getResource("/swagger.json");

        } else if (variant.getMediaType() == TEXT_HTML_TYPE) {
            url = getClass().getResource("/swagger.html");

        } else {

            return Response.status(Status.BAD_REQUEST).build();
        }

        return Response.ok(url.openStream(), variant.getMediaType()).build();

    }

}
