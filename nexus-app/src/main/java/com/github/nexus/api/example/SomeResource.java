package com.github.nexus.api.example;

import com.github.api.nexus.quorum.v1.SomeObject;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Demonstration of an endpoint that uses a data object which is dynamically generated from an xsd.
 */
@Path("/some")
public class SomeResource {
    
    private static final Logger LOGGER = Logger.getLogger(SomeResource.class.getName());
    
   @POST
    @Path("thing")
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response save(final SomeObject someObject) {
        LOGGER.log(Level.INFO,"{0}",Objects.toString(someObject));
        
        return Response.status(Response.Status.CREATED).build();
    }
    
    
    @POST
    @Path("else")
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response save(@Valid final SomeOtherObject someObject) {
        LOGGER.log(Level.INFO,"{0}",Objects.toString(someObject));
        
        return Response.status(Response.Status.CREATED).build();
    }
    
}
