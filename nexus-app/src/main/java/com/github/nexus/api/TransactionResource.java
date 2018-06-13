package com.github.nexus.api;

import com.github.nexus.api.model.*;
import com.github.nexus.enclave.Enclave;
import com.github.nexus.util.Base64Decoder;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Base64;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

@Path("/transaction")
public class TransactionResource {

    private static final Logger LOGGER = Logger.getLogger(TransactionResource.class.getName());

    private final Enclave enclave;
    private final Base64Decoder base64Decoder;
    
    public TransactionResource(final Enclave enclave,final Base64Decoder base64Decoder) {
        this.enclave = requireNonNull(enclave, "enclave must not be null");
        this.base64Decoder = requireNonNull(base64Decoder, "decoder must not be null");
    }

    @POST
    @Path("/send")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON})
    public Response send(@Valid final SendRequest sendRequest) {

        byte[] from = base64Decoder.decode(sendRequest.getFrom());
            
        byte[][] recipients =
            Stream.of(sendRequest.getTo())
                .map(x -> base64Decoder.decode(x))
                .toArray(byte[][]::new);
            
        byte[] payload = base64Decoder.decode(sendRequest.getPayload());

        byte[] key = enclave.store(from, recipients, payload).getHashBytes();

        String encodedKey = base64Decoder.encodeToString(key);
        SendResponse response = new SendResponse(encodedKey);

        return Response.status(Response.Status.CREATED)
            .header("Content-Type", "application/json")
            .entity(response)
            .build();

    }

    @POST
    @Path("/receive")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response receive(@Valid final ReceiveRequest receiveRequest) {

        byte[] key = base64Decoder.decode(receiveRequest.getKey());

        byte[] to = base64Decoder.decode(receiveRequest.getTo());

        byte[] payload = enclave.receive(key, to);

        String encodedPayload = base64Decoder.encodeToString(payload);

        ReceiveResponse response = new ReceiveResponse(encodedPayload);

        return Response.status(Response.Status.CREATED)
            .header("Content-Type", "application/json")
            .entity(response)
            .build();

    }

    @POST
    @Path("/delete")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response delete(@Valid final DeleteRequest deleteRequest) {

        byte[] hashBytes = base64Decoder.decode(deleteRequest.getKey());

        enclave.delete(hashBytes);

        return Response.status(Response.Status.OK)
            .entity("Delete successful")
            .build();

    }

    @POST
    @Path("/resend")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response resend(@Valid final ResendRequest resendRequest) {
        String type = resendRequest.getType();
        byte[] publickey = Base64.getDecoder().decode(resendRequest.getPublicKey());

        if (type.equalsIgnoreCase(ResendRequestType.ALL.name())) {
            LOGGER.info("ALL");
        } else if (type.equalsIgnoreCase(ResendRequestType.INDIVIDUAL.name())) {
            byte[] key = Base64.getDecoder().decode(resendRequest.getKey());
            LOGGER.info("INDIVIDUAL");

        }
        return Response.status(Response.Status.CREATED).build();
    }

    @POST
    @Path("/push")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public Response push(final byte[] payload) {
        LOGGER.info(Base64.getEncoder().encodeToString(enclave.storePayload(payload).getHashBytes()));

        return Response.status(Response.Status.CREATED).build();
    }


}
