package com.github.nexus.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/upcheck")
public class UpCheckResource {

    private static final Logger LOGGER = Logger.getLogger(UpCheckResource.class.getName());

    private static final String UPCHECK_RESPONSE = "I'm up!";

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String upCheck() {
        LOGGER.log(Level.INFO,"GET upcheck");

        return UPCHECK_RESPONSE;
    }
}
