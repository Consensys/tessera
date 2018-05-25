package com.github.nexus.api;

import com.github.nexus.api.model.DeleteRequest;
import com.github.nexus.api.model.ReceiveRequest;
import com.github.nexus.api.model.ResendRequest;
import com.github.nexus.api.model.SendRequest;
import com.github.nexus.service.TransactionService;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/transaction")
public class TransactionResource {

    private static final Logger LOGGER = Logger.getLogger(TransactionResource.class.getName());

    private TransactionService transactionService;

    public TransactionResource(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @POST
    @Path("/send")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response send(@Valid final SendRequest sendRequest){
        LOGGER.log(Level.INFO,"POST send");
        transactionService.send();
        return Response.status(Response.Status.CREATED).build();
    }

    @POST
    @Path("/receive")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response receive(@Valid final ReceiveRequest receiveRequest){
        LOGGER.log(Level.INFO,"POST receive");
        transactionService.receive();
        return Response.status(Response.Status.CREATED).build();
    }

    @POST
    @Path("/delete")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response delete(@Valid final DeleteRequest deleteRequest){
        LOGGER.log(Level.INFO,"POST delete");
        transactionService.delete();
        return Response.status(Response.Status.CREATED).build();
    }

    @POST
    @Path("/resend")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response delete(@Valid final ResendRequest resendRequest){
        LOGGER.log(Level.INFO,"POST resend");
        transactionService.resend();
        return Response.status(Response.Status.CREATED).build();
    }


}
