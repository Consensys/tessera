package com.quorum.tessera.api.common;

import com.quorum.tessera.api.Version;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Provides endpoints to determine versioning information */
@Path("/version")
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
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Request current version of P2PRestApp")
    @ApiResponses({@ApiResponse(code = 200, message = "Current application version ", response = String.class)})
    public String getVersion() {

        LOGGER.info("GET version");

        return VERSION;
    }
}
