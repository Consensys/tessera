package com.quorum.tessera.api.common;

import com.quorum.tessera.api.Version;
import com.quorum.tessera.version.ApiVersion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;

import javax.json.Json;
import javax.json.JsonArray;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/** Provides endpoints to determine versioning information */
@Tags({@Tag(name = "quorum-to-tessera"), @Tag(name = "peer-to-peer"), @Tag(name = "third-party")})
@Path("/")
public class VersionResource {

    private static final String VERSION = Version.getVersion();

    @Deprecated
    @Operation(summary = "/version", description = "Tessera distribution version")
    @ApiResponse(
            responseCode = "200",
            description = "Tessera distribution version",
            content = @Content(schema = @Schema(type = "string"), examples = @ExampleObject(value = "0.11.0")))
    @GET
    @Path("version")
    @Produces(MediaType.TEXT_PLAIN)
    public String getVersion() {
        return VERSION;
    }

    @Operation(summary = "/version/distribution", description = "Tessera distribution version")
    @ApiResponse(
            responseCode = "200",
            description = "Tessera distribution version",
            content = @Content(schema = @Schema(type = "string"), examples = @ExampleObject(value = "0.11.0")))
    @GET
    @Path("version/distribution")
    @Produces(MediaType.TEXT_PLAIN)
    public String getDistributionVersion() {
        return VERSION;
    }

    @Operation(
            summary = "/version/api",
            operationId = "getApiVersions",
            description = "list currently available API versions")
    @ApiResponse(
            responseCode = "200",
            description = "list of available API versions",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            array = @ArraySchema(schema = @Schema(type = "string")),
                            examples = @ExampleObject("[\"1.0\",\"2.0\"]")))
    @GET
    @Path("version/api")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonArray getVersions() {
        List<String> versions =
                ApiVersion.versions().stream()
                        .map(version -> version.substring(1)) // remove the "v" prefix
                        .map(Double::parseDouble)
                        .sorted()
                        .map(Objects::toString)
                        .collect(Collectors.toList());

        return Json.createArrayBuilder(versions).build();
    }
}
