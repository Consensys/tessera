package com.github.nexus.api;

import com.github.nexus.Base64Decoder;
import com.github.nexus.api.exception.DecodingException;
import com.github.nexus.api.model.*;
import com.github.nexus.enclave.Enclave;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
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
    public Response send(@Valid final SendRequest sendRequest) throws DecodingException {
    
            byte[] from = base64Decoder.decode(sendRequest.getFrom());
            
            byte[][] recipients =
                Stream.of(sendRequest.getTo())
                            .map(x -> base64Decoder.decode(x))
                            .toArray(byte[][]::new);
            
            byte[] payload = base64Decoder.decode(sendRequest.getPayload());

            byte[] key = enclave.store(from, recipients, payload);

            String encodedKey = base64Decoder.encodeToString(key);
            SendResponse response = new SendResponse(encodedKey);

            return Response.status(Response.Status.CREATED)
                    .header("Content-Type", "application/json")
                    .entity(response)
                    .build();

    }

    @POST
    @Path("/sendraw")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response sendRaw(@Context final HttpHeaders headers, InputStream inputStream) throws IOException {
        LOGGER.log(Level.INFO, "from: {0}", headers.getHeaderString("hFrom"));
        List<String> hTo = headers.getRequestHeader("hTo");
        LOGGER.log(Level.INFO, "to: {0}", hTo);
        LOGGER.log(Level.INFO, "payload: {0}", readInputStream(inputStream));
        return Response.status(Response.Status.CREATED).build();
    }

    @POST
    @Path("/receive")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response receive(@Valid final ReceiveRequest receiveRequest) throws DecodingException {
  
            byte[] key = base64Decoder.decode(receiveRequest.getKey());

            byte[] to = base64Decoder.decode(receiveRequest.getTo());

            //TODO Call enlave retrieve here
            byte[] payload = "Retrieved payload".getBytes();

            String encodedPayload = base64Decoder.encodeToString(payload);
            ReceiveResponse response = new ReceiveResponse(encodedPayload);

            return Response.status(Response.Status.CREATED)
                    .header("Content-Type", "application/json")
                    .entity(response)
                    .build();

    }

    @POST
    @Path("/receiveraw")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response receiveRaw(@Context final HttpHeaders headers) {
        LOGGER.log(Level.INFO, "from: {0}", headers.getHeaderString("hKey"));
        LOGGER.log(Level.INFO, "to: {0}", headers.getHeaderString("hTo"));
        return Response.status(Response.Status.CREATED).build();
    }

    @POST
    @Path("/delete")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response delete(@Valid final DeleteRequest deleteRequest) {

        byte[] key = Base64.getDecoder().decode(deleteRequest.getKey());

        return Response.status(Response.Status.CREATED).build();
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
    public Response push(final InputStream inputStream) throws IOException {

        byte[] payload = Base64.getDecoder().decode(readInputStream(inputStream));

        return Response.status(Response.Status.CREATED).build();
    }

    protected static String readInputStream(InputStream inputStream) throws IOException {

        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(inputStream))) {
            return buffer.lines().collect(Collectors.joining("\n"));
        }
    }

}
