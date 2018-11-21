package com.quorum.tessera.p2p;

import com.quorum.tessera.api.filter.Logged;
import com.quorum.tessera.transaction.TransactionManager;
import com.quorum.tessera.api.model.*;
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
import com.quorum.tessera.enclave.model.MessageHash;

/**
 * Provides endpoints for dealing with transactions, including:
 *
 * - creating new transactions and distributing them - deleting transactions -
 * fetching transactions - resending old transactions
 */
@Logged
@Path("/")
public class TransactionResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionResource.class);

    private final TransactionManager delegate;

    public TransactionResource(TransactionManager delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Deprecated
    @ApiOperation("Deprecated: Replaced by /transaction/{key} DELETE HTTP method")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Status message", response = String.class),
        @ApiResponse(code = 404, message = "If the entity doesn't exist")
    })
    @POST
    @Path("delete")
    @Consumes(APPLICATION_JSON)
    @Produces(TEXT_PLAIN)
    public Response delete(
            @ApiParam(name = "deleteRequest", required = true, value = "Key data to be deleted")
            @Valid final DeleteRequest deleteRequest) {

        LOGGER.debug("Received deprecated delete request");

        delegate.delete(deleteRequest);

        return Response.status(Response.Status.OK)
                .entity("Delete successful")
                .build();
    }

    @ApiOperation("Delete single transaction from P2PRestApp node")
    @ApiResponses({
        @ApiResponse(code = 204, message = "Successful deletion"),
        @ApiResponse(code = 404, message = "If the entity doesn't exist")
    })
    @DELETE
    @Path("/transaction/{key}")
    public Response deleteKey(@ApiParam("Encoded hash") @PathParam("key") final String key) {

        LOGGER.debug("Received delete key request");

        DeleteRequest delete = new DeleteRequest();
        delete.setKey(key);
        delegate.delete(delete);

        return Response.noContent().build();
    }

    @ApiOperation("Resend transactions for given key or message hash/recipient")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Encoded payload when TYPE is INDIVIDUAL", response = String.class),
        @ApiResponse(code = 500, message = "General error")
    })
    @POST
    @Path("resend")
    @Consumes(APPLICATION_JSON)
    @Produces(TEXT_PLAIN)
    public Response resend(
            @ApiParam(name = "resendRequest", required = true) @Valid @NotNull final ResendRequest resendRequest
    ) {

        LOGGER.debug("Received resend request");

        ResendResponse response = delegate.resend(resendRequest);
        Response.ResponseBuilder builder = Response.status(Status.OK);
        response.getPayload().ifPresent(builder::entity);
        return builder.build();

    }

    @ApiOperation(value = "Transmit encrypted payload between P2PRestApp Nodes")
    @ApiResponses({
        @ApiResponse(code = 201, message = "Key created status"),
        @ApiResponse(code = 500, message = "General error")
    })
    @POST
    @Path("push")
    @Consumes(APPLICATION_OCTET_STREAM)
    public Response push(
            @ApiParam(name = "payload", required = true, value = "Key data to be stored.") final byte[] payload
    ) {

        LOGGER.debug("Received push request");

        final MessageHash messageHash = delegate.storePayload(payload);
        LOGGER.debug("Push request generated hash {}", Objects.toString(messageHash));
        //TODO: Return the query url not the string of the messageHAsh
        return Response.status(Response.Status.CREATED)
                .entity(Objects.toString(messageHash))
                .build();
    }

}
