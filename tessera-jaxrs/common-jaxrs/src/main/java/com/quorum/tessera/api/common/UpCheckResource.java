package com.quorum.tessera.api.common;

import com.quorum.tessera.transaction.TransactionManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/** Provides endpoints about the health status of this node */
@Api
@Path("/upcheck")
public class UpCheckResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpCheckResource.class);

    private static final String UPCHECK_RESPONSE_IS_UP = "I'm up!";
    private static final String UPCHECK_RESPONSE_DB = "Database unavailable";

    private final TransactionManager transactionManager;

    public UpCheckResource(final TransactionManager transactionManager) {
        this.transactionManager = Objects.requireNonNull(transactionManager);
    }

    /**
     * Called to check if the application is running and responsive. Gives no details about the health of the
     * application other than it is up.
     *
     * @return a string stating the application is running
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @ApiResponses({
        @ApiResponse(code = 200, message = UPCHECK_RESPONSE_IS_UP),
        @ApiResponse(code = 200, message = UPCHECK_RESPONSE_DB)
    })
    @ApiOperation(value = "Check if local P2PRestApp Node is up")
    public Response upCheck() {
        LOGGER.info("GET upcheck");

        if (!transactionManager.upcheck()) {
            return Response.ok(UPCHECK_RESPONSE_DB).build();
        }

        return Response.ok(UPCHECK_RESPONSE_IS_UP).build();
    }
}
