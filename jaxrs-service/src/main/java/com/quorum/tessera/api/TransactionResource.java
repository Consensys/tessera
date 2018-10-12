package com.quorum.tessera.api;

import com.quorum.tessera.api.filter.Logged;
import com.quorum.tessera.transaction.TransactionManager;
import com.quorum.tessera.api.filter.PrivateApi;
import com.quorum.tessera.api.model.*;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Objects;
import java.util.Optional;
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

    @ApiOperation(value = "Send private transaction payload", produces = "Encrypted payload")
    @ApiResponses({
        @ApiResponse(code = 200, response = SendResponse.class, message = "Send response"),
        @ApiResponse(code = 400, message = "For unknown and unknown keys")
    })
    @POST
    @PrivateApi
    @Path("send")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response send(
            @ApiParam(name = "sendRequest", required = true)
            @NotNull @Valid final SendRequest sendRequest) {

        //TODO: Hand cranking decoding will be fixed using jaxrs rather than manually
        SendRequest amendSendRequest = new SendRequest();
        amendSendRequest.setFrom(sendRequest.getFrom());
        amendSendRequest.setTo(sendRequest.getTo());
        
        byte[] decodedPayload = Base64.getDecoder().decode(sendRequest.getPayload());
        amendSendRequest.setPayload(decodedPayload);
        
        final SendResponse response = delegate.send(amendSendRequest);

        return Response.status(Response.Status.OK)
                .type(APPLICATION_JSON)
                .entity(response)
                .build();

    }

    @ApiOperation(value = "Send private transaction payload", produces = "Encrypted payload")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Encoded Key", response = String.class),
        @ApiResponse(code = 500, message = "Unknown server error")
    })
    @POST
    @PrivateApi
    @Path("sendraw")
    @Consumes(APPLICATION_OCTET_STREAM)
    @Produces(TEXT_PLAIN)
    public Response sendRaw(
            @HeaderParam("c11n-from") final String sender,
            @HeaderParam("c11n-to") final String recipientKeys,
            @NotNull @Size(min = 1) final byte[] payload) {

        SendRequest sendRequest = new SendRequest();
        sendRequest.setFrom(sender);

        sendRequest.setPayload(payload);

        Optional.ofNullable(recipientKeys)
                .filter(s -> !Objects.equals("", s))
                .map(v -> v.split(","))
                .ifPresent(sendRequest::setTo);

        final SendResponse sendResponse = delegate.send(sendRequest);

        final String encodedKey = sendResponse.getKey();

        LOGGER.debug("Encoded key: {}", encodedKey);

        //TODO: Quorum expects only 200 responses. When Quorum can handle a 201, change to CREATED
        return Response.status(Response.Status.OK)
                .entity(encodedKey)
                .build();
    }

    @ApiOperation(value = "Returns decrypted payload back to Quorum")
    @ApiResponses({
        @ApiResponse(code = 200, response = ReceiveResponse.class, message = "Receive Response object")
    })
    @GET
    @PrivateApi
    @Path("/transaction/{hash}")
    @Produces(APPLICATION_JSON)
    public Response receive(
            @ApiParam("Encoded hash used to decrypt the payload")
            @NotNull @Valid @PathParam("hash") final String hash,
            @ApiParam("Encoded recipient key")
            @Valid @QueryParam("to") final String toStr
    ) {

        ReceiveRequest receiveRequest = new ReceiveRequest();
        receiveRequest.setKey(hash);
        receiveRequest.setTo(toStr);
        ReceiveResponse response = delegate.receive(receiveRequest);

        return Response.status(Response.Status.OK)
                .type(APPLICATION_JSON)
                .entity(response)
                .build();

    }

    @GET
    @PrivateApi
    @Path("/receive")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response receive(@Valid final ReceiveRequest request) {

        LOGGER.debug("Received receive request");

        ReceiveResponse response = delegate.receive(request);

        return Response.status(Response.Status.OK)
                .type(APPLICATION_JSON)
                .entity(response)
                .build();
    }

    @ApiOperation(value = "Submit keys to retrieve payload and decrypt it", produces = "Unencrypted payload")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Raw payload", response = byte[].class)})
    @GET
    @Path("receiveraw")
    @Consumes(APPLICATION_OCTET_STREAM)
    @Produces(APPLICATION_OCTET_STREAM)
    public Response receiveRaw(
            @ApiParam("Encoded transaction hash")
            @NotNull @HeaderParam(value = "c11n-key") String hash,
            @ApiParam("Encoded Recipient Public Key")
            @HeaderParam(value = "c11n-to") String recipientKey) {

        LOGGER.debug("Received receiveraw request");

        ReceiveRequest receiveRequest = new ReceiveRequest();
        receiveRequest.setKey(hash);
        receiveRequest.setTo(recipientKey);

        ReceiveResponse receiveResponse = delegate.receive(receiveRequest);

        byte[] payload = receiveResponse.getPayload();

        return Response.status(Response.Status.OK)
                .entity(payload)
                .build();
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

    @ApiOperation("Delete single transaction from Tessera node")
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

    @ApiOperation(value = "Transmit encrypted payload between Tessera Nodes")
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
