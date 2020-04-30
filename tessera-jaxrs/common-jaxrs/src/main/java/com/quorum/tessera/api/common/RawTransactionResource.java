package com.quorum.tessera.api.common;

import com.quorum.tessera.api.model.StoreRawRequest;
import com.quorum.tessera.api.model.StoreRawResponse;
import com.quorum.tessera.transaction.TransactionManager;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.Objects;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/** Provides endpoints for dealing with raw transactions */
@Api
@Path("/")
public class RawTransactionResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(RawTransactionResource.class);

    public static final String ENDPOINT_STORE_RAW = "storeraw";

    private final TransactionManager delegate;

    public RawTransactionResource(final TransactionManager delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    @ApiOperation(value = "Store raw private transaction payload")
    @ApiResponses({
        @ApiResponse(code = 200, response = StoreRawResponse.class, message = "Store response"),
        @ApiResponse(code = 400, message = "For unknown sender")
    })
    @POST
    @Path(ENDPOINT_STORE_RAW)
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response store(
            @ApiParam(name = "storeRawRequest", required = true) @NotNull @Valid final StoreRawRequest request) {
        final StoreRawResponse response = delegate.store(request);

        return Response.ok().type(APPLICATION_JSON).entity(response).build();
    }
}
