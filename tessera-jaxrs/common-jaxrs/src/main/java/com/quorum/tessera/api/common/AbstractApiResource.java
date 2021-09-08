package com.quorum.tessera.api.common;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.*;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

/** Provides HTTP endpoints for accessing OpenAPI schema */
@Tags({@Tag(name = "quorum-to-tessera"), @Tag(name = "peer-to-peer"), @Tag(name = "third-party")})
@Path("/api")
public abstract class AbstractApiResource {

  private static final String APPLICATION_YAML = "application/yaml";

  private static final MediaType APPLICATION_YAML_TYPE = new MediaType("application", "yaml");

  @Operation(
      summary = "/api",
      method = "get",
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
  public Response api(@Context Request request) throws IOException {
    final Optional<MediaType> mediaType =
        Optional.ofNullable(request.selectVariant(getVariants())).map(Variant::getMediaType);

    final Optional<String> mediaSubType = mediaType.map(MediaType::getSubtype);

    if (mediaSubType.isPresent()) {
      final String resourceName = String.format("/%s.%s", getOpenApiDocName(), mediaSubType.get());
      final URL url = getResourceUrl(resourceName);

      return Response.ok(url.openStream(), mediaType.get()).build();
    } else {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
  }

  public List<Variant> getVariants() {
    return Variant.mediaTypes(APPLICATION_JSON_TYPE, APPLICATION_YAML_TYPE).build();
  }

  public URL getResourceUrl(String name) {
    return getClass().getResource(name);
  }

  public abstract String getOpenApiDocName();
}
