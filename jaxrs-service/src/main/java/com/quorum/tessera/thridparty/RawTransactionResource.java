package com.quorum.tessera.thridparty;

import com.quorum.tessera.api.filter.DomainFilter;
import com.quorum.tessera.api.filter.Logged;
import com.quorum.tessera.api.model.*;
import com.quorum.tessera.config.apps.ThirdPartyApp;
import com.quorum.tessera.transaction.TransactionManager;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Objects;

import static javax.ws.rs.core.MediaType.*;

/**
 * Provides endpoints for dealing with raw transactions
 */
@DomainFilter
@Logged
@Path("/")
public class RawTransactionResource implements ThirdPartyApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(RawTransactionResource.class);

    private final TransactionManager delegate;

    public RawTransactionResource(TransactionManager delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    @ApiOperation(value = "Store raw private transaction payload", produces = "Encrypted payload")
    @ApiResponses({
        @ApiResponse(code = 200, response = StoreRawResponse.class, message = "Store response"),
        @ApiResponse(code = 400, message = "For unknown sender")
    })
    @POST
    @Path("storeraw")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response store(
            @ApiParam(name = "storeRawRequest", required = true)
            @NotNull @Valid final StoreRawRequest storeRawRequest) {

        final StoreRawResponse response = delegate.store(storeRawRequest);

        return Response.status(Status.OK)
                .type(APPLICATION_JSON)
                .entity(response)
                .build();

    }
}
