package com.github.nexus.api.example;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Public API entry points
 */
@Path("/")
public class PublicAPI {

    private static final Logger LOGGER = Logger.getLogger(PublicAPI.class.getName());

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("version")
    public String version() {
        LOGGER.log(Level.INFO,"GET version");

        return "No version defined yet!";
    }
}
