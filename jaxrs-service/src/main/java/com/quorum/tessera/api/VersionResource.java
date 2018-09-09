package com.quorum.tessera.api;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides endpoints to determine versioning information
 */
@Path("/version")
public class VersionResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(VersionResource.class);

    private static final String VERSION = "No version defined yet!";

    /**
     * An endpoint describing the current version of the application
     *
     * @return the version of the application
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Request current version of Tessera", produces = "current version number")
    @ApiResponses({@ApiResponse(code = 200, message = "Current application version ", response = String.class)})
    public String getVersion() {

        LOGGER.info("GET version");

        return VERSION;
    }
}
