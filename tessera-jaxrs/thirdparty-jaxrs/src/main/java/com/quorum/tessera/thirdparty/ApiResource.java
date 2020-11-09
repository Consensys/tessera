package com.quorum.tessera.thirdparty;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;

import javax.servlet.ServletConfig;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.*;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

/** Provides HTTP endpoints for accessing the OpenAPI schema */
@Tags({@Tag(name = "quorum-to-tessera"), @Tag(name = "peer-to-peer"), @Tag(name = "third-party")})
@Path("/api")
public class ApiResource {

    private static final String APPLICATION_YAML = "application/yaml";

    private static final MediaType APPLICATION_YAML_TYPE = new MediaType("application", "yaml");

    @Operation(
            summary = "/api",
            description = "returns JSON or HTML OpenAPI document",
            operationId = "getOpenApiDocument")
    @ApiResponse(
            responseCode = "200",
            description = "JSON or HTML OpenAPI document",
            content = {
                @Content(
                        mediaType = MediaType.APPLICATION_JSON,
                        schema = @Schema(description = "JSON OpenAPI document", type = "string")),
                @Content(
                        mediaType = APPLICATION_YAML,
                        schema = @Schema(description = "YAML OpenAPI document", type = "string"))
            })
    @ApiResponse(responseCode = "400", description = "Unsupported mediaType")
    @GET
    public Response api(
            @Context HttpHeaders headers,
            @Context ServletConfig config,
            @Context Application app,
            @Context UriInfo uriInfo,
            @Context Request request)
            throws Exception {
        //        final Optional<MediaType> mediaType =
        // Optional.ofNullable(request.selectVariant(variants())).map(Variant::getMediaType);
        //
        //        final Optional<String> mediaSubType = mediaType.map(MediaType::getSubtype);
        //
        //        if (mediaSubType.isPresent()) {
        //            final URL url;
        //            if (mediaSubType.filter("json"::equals).isPresent()) {
        //                url = getClass().getResource("/swagger.json");
        //            } else {
        //                url = getClass().getResource("/swagger.html");
        //            }
        //            return Response.ok(url.openStream(), mediaType.get()).build();
        //        } else {
        //            return Response.status(Response.Status.BAD_REQUEST).build();
        //        }
        return Response.ok("thirdparty openapi doc").build();
    }

    static List<Variant> variants() {
        return Variant.mediaTypes(APPLICATION_JSON_TYPE, APPLICATION_YAML_TYPE).build();
    }
}
