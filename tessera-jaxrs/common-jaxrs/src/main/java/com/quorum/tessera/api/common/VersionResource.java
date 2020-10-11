package com.quorum.tessera.api.common;

import com.quorum.tessera.api.Version;
import com.quorum.tessera.version.ApiVersion;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.json.*;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/** Provides endpoints to determine versioning information */
@Path("/")
@Api
public class VersionResource {


    @GET
    @Path("version")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Request distribution version of Tessera")
    @ApiResponses({@ApiResponse(code = 200, message = "Current api version ", response = String.class)})
    public String getVersion() {
        List<String> versions = versions();
        return versions.get(versions.size() - 1);
    }

    @GET
    @Path("versions")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Request all API versions available")
    @ApiResponses({@ApiResponse(code = 200, message = "All supported api versions", response = JsonArray.class)})
    public JsonArray getVersions() {
        return Json.createArrayBuilder(versions()).build();
    }

    @GET
    @Path("version/info")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "All version info")
    @ApiResponses({@ApiResponse(code = 200, message = "All version info", response = JsonObject.class)})
    public JsonObject getInfo() {
        return Json.createObjectBuilder()
            .add("versions",Json.createArrayBuilder(versions()))
            .add("dist",Version.getVersion()).build();


    }

    private static List<String> versions() {
       return ApiVersion.versions()
            .stream()
            .map(version -> version.substring(1)) //remove the "v" prefix
            .map(Double::parseDouble)
            .sorted()
            .map(Objects::toString)
            .collect(Collectors.toList());
    }

}
