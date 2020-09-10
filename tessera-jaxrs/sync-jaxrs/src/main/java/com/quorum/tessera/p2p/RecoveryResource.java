package com.quorum.tessera.p2p;

import com.quorum.tessera.p2p.recovery.PushBatchRequest;
import com.quorum.tessera.recovery.workflow.BatchResendManager;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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

        com.quorum.tessera.recovery.resend.PushBatchRequest request =
                com.quorum.tessera.recovery.resend.PushBatchRequest.from(pushBatchRequest.getEncodedPayloads());

        batchResendManager.storeResendBatch(request);

        LOGGER.debug("Push batch processed successfully");
        return Response.status(Response.Status.OK).build();
    }

    //    @ApiOperation(value = "Transmit encrypted payload between P2PRestApp Nodes")
    //    @ApiResponses({
    //        @ApiResponse(code = 201, message = "Key created status"),
    //        @ApiResponse(code = 500, message = "General error")
    //    })
    //    @POST
    //    @Path("push")
    //    @Consumes(APPLICATION_OCTET_STREAM)
    //    public Response push(
    //            @ApiParam(name = "payload", required = true, value = "Key data to be stored.") final byte[] payload) {
    //
    //        LOGGER.debug("Received a push from legacy Tessera data recovery flow");
    //
    //        final PushBatchRequest request = new PushBatchRequest();
    //        request.setEncodedPayloads(List.of(payload));
    //
    //        batchResendManager.storeResendBatch(request);
    //
    //        LOGGER.debug("Push transaction processed successfully");
    //        return Response.status(Response.Status.OK).build();
    //    }
}
