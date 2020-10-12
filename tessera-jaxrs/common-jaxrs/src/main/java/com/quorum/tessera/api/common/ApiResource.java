package com.quorum.tessera.api.common;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;

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

/** Provides HTTP endpoints for accessing the OpenAPI schema */
@Tags({@Tag(name = "quorum-to-tessera"), @Tag(name = "peer-to-peer"), @Tag(name = "third-party")})
@Path("/api")
public class ApiResource {

    private static final List<Variant> VARIANTS = Variant.mediaTypes(APPLICATION_JSON_TYPE, TEXT_HTML_TYPE).build();

    @Operation(
            summary = "/api",
            description = "returns JSON or HTML OpenAPI document",
            operationId = "getOpenApiDocument")
    @ApiResponse(
            responseCode = "200",
            description = "JSON or HTML OpenAPI document",
            content = {
                @Content(
                        mediaType = APPLICATION_JSON,
                        schema = @Schema(description = "JSON OpenAPI document", type = "string")),
                @Content(
                        mediaType = TEXT_HTML,
                        schema = @Schema(description = "HTML OpenAPI document", type = "string"))
            })
    @ApiResponse(responseCode = "400", description = "Unsupported mediaType")
    @GET
    @Produces({APPLICATION_JSON, TEXT_HTML})
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
