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
 * Provides endpoints about the health status of this node
 */
@Path("/upcheck")
public class UpCheckResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpCheckResource.class);

    private static final String UPCHECK_RESPONSE = "I'm up!";

    /**
     * Called to check if the application is running and responsive
     * Gives no details about the health of the application other than it is up
     *
     * @return a string stating the application is running
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @ApiResponses({@ApiResponse(code = 200, message = UPCHECK_RESPONSE)})
    @ApiOperation(value = "Check if local Tessera Node is up", produces = "I'm up")
    public String upCheck() {

        LOGGER.info("GET upcheck");

        return UPCHECK_RESPONSE;
    }
}
