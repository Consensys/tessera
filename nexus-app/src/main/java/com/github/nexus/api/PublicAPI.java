package com.github.nexus.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

//FIXME: Should follow restful resource conventions
/**
 * Public API entry points
 */
@Path("/api")
public class PublicAPI {

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("version")
    public String getIt() {
        return "No version defined yet!";
    }
}
