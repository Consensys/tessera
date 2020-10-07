package com.quorum.tessera.api.common;

import com.quorum.tessera.api.Version;
import com.quorum.tessera.version.ApiVersion;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.*;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

/** Provides endpoints to determine versioning information */
@Path("/")
@Api
public class VersionResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(VersionResource.class);

    private static final String VERSION = Version.getVersion();

    /**
     * An endpoint describing the current version of the application
     *
     * @return the version of the application
     */
    @GET
    @Path("version")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Request current version of P2PRestApp")
    @ApiResponses({@ApiResponse(code = 200, message = "Current application version ", response = String.class)})
    public String getVersion() {
        LOGGER.info("GET version");
        return VERSION;
    }

    @GET
    @Path("version/api")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Request current version of P2PRestApp")
    @ApiResponses({@ApiResponse(code = 200, message = "All supported api versions", response = JsonArray.class)})
    public JsonObject getVersions() {
        final List<JsonObjectBuilder> sortedAllVersions = ApiVersion.versions()
            .stream()
            .map(version -> version.substring(1)) //remove the "v" prefix
            .map(Double::parseDouble)
            .sorted()
            .map(Object::toString)
            .map(version -> Json.createObjectBuilder().add("version", version))
            .collect(Collectors.toList());

        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        sortedAllVersions.forEach(arrayBuilder::add);

        return Json.createObjectBuilder().add("versions", arrayBuilder).build();
    }
}
