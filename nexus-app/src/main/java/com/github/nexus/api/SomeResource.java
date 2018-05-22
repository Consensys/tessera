
package com.github.nexus.api;

import com.github.api.nexus.quorum.v1.SomeObject;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/some")
public class SomeResource {
    
    private static final Logger LOGGER = Logger.getLogger(SomeResource.class.getName());
    
   @POST
    @Path("thing")
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response transfer(final SomeObject someObject) {
        LOGGER.log(Level.INFO,"{0}",Objects.toString(someObject));
        
        return Response.status(Response.Status.CREATED).build();
    }
    
}
