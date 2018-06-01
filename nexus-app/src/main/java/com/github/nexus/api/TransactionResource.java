package com.github.nexus.api;

import com.github.nexus.api.model.*;
import com.github.nexus.service.TransactionService;

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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import java.util.stream.Stream;

@Path("/transaction")
public class TransactionResource {

    private static final Logger LOGGER = Logger.getLogger(TransactionResource.class.getName());

    private TransactionService transactionService;

    public TransactionResource(final TransactionService transactionService) {
        this.transactionService = requireNonNull(transactionService, "transactionService must not be null");
    }

    @POST
    @Path("/send")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON})
    public Response send(@Valid final SendRequest sendRequest) {
        byte[] payload = Base64.getDecoder().decode(sendRequest.getPayload());
        byte[] key = transactionService.send();
        String encodedKey = Base64.getEncoder().encodeToString(key);
        SendResponse response = new SendResponse(encodedKey);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @POST
    @Path("/sendraw")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response sendRaw(@Context final HttpHeaders headers, InputStream inputStream) throws IOException {
        LOGGER.log(Level.INFO, "from: {0}", headers.getHeaderString("hFrom"));
        LOGGER.log(Level.INFO, "to: {0}", headers.getRequestHeader("hTo").toArray());
        LOGGER.log(Level.INFO, "payload: {0}", readInputStream(inputStream));
        transactionService.send();
        return Response.status(Response.Status.CREATED).build();
    }

    @POST
    @Path("/receive")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response receive(@Valid final ReceiveRequest receiveRequest) {
        LOGGER.log(Level.INFO, "POST receive");
        transactionService.receive();
        return Response.status(Response.Status.CREATED).build();
    }

    @POST
    @Path("/receiveraw")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response receiveRaw(@Context final HttpHeaders headers) {
        LOGGER.log(Level.INFO, "from: {0}", headers.getHeaderString("hKey"));
        LOGGER.log(Level.INFO, "to: {0}", headers.getHeaderString("hTo"));
        transactionService.receive();
        return Response.status(Response.Status.CREATED).build();
    }

    @POST
    @Path("/delete")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response delete(@Valid final DeleteRequest deleteRequest) {
        LOGGER.log(Level.INFO, "POST delete");
        transactionService.delete();
        return Response.status(Response.Status.CREATED).build();
    }

    @POST
    @Path("/resend")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response resend(@Valid final ResendRequest resendRequest) {
        LOGGER.log(Level.INFO, "POST resend");
        transactionService.resend();
        return Response.status(Response.Status.CREATED).build();
    }

    @POST
    @Path("/push")
    public Response push(final InputStream payload) throws IOException {
        LOGGER.log(Level.INFO, "payload: {0}", readInputStream(payload));
        transactionService.push();
        return Response.status(Response.Status.CREATED).build();
    }

    @POST
    @Path("/partyinfo")
    public Response partyInfo(final InputStream payload) throws IOException {
        LOGGER.log(Level.INFO, "payload: {0}", readInputStream(payload));
        return Response.status(Response.Status.CREATED).build();
    }

    protected static String readInputStream(InputStream inputStream) throws IOException {

        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(inputStream))) {
            return buffer.lines().collect(Collectors.joining("\n"));
        }
    }

}
