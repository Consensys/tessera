package com.quorum.tessera.p2p;

import com.quorum.tessera.partyinfo.PushBatchRequest;
import com.quorum.tessera.partyinfo.ResendBatchRequest;
import com.quorum.tessera.partyinfo.ResendBatchResponse;
import com.quorum.tessera.recover.resend.BatchResendManager;
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

@Api
@Path("/")
public class RecoveryResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecoveryResource.class);

    private final BatchResendManager batchResendManager;

    public RecoveryResource(BatchResendManager batchResendManager) {
        this.batchResendManager = Objects.requireNonNull(batchResendManager);
    }

    @ApiOperation(value = "Transmit encrypted payload batches between P2PRestApp Nodes")
    @ApiResponses({
        @ApiResponse(code = 200, message = "when the batch is stored successfully"),
        @ApiResponse(code = 500, message = "General error")
    })
    @POST
    @Path("pushBatch")
    @Consumes(APPLICATION_JSON)
    public Response pushBatch(
            @ApiParam(name = "pushBatchRequest", required = true, value = "The batch of transactions.") @Valid @NotNull
                    final PushBatchRequest pushBatchRequest) {

        LOGGER.debug("Received push request");

        batchResendManager.storeResendBatch(pushBatchRequest);

        LOGGER.debug("Push batch processed successfully");
        return Response.status(Response.Status.OK).build();
    }

    @ApiOperation("Resend transaction batches for given recipient key")
    @ApiResponses({
        @ApiResponse(code = 200, message = "The transaction total that has been pushed", response = String.class),
        @ApiResponse(code = 500, message = "General error")
    })
    @POST
    @Path("resendBatch")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response resendBatch(
            @ApiParam(name = "resendBatchRequest", required = true) @Valid @NotNull
                    final ResendBatchRequest resendBatchRequest) {

        LOGGER.debug("Received resend request");

        ResendBatchResponse response = batchResendManager.resendBatch(resendBatchRequest);
        Response.ResponseBuilder builder = Response.status(Response.Status.OK);
        builder.entity(response);
        return builder.build();
    }
}
