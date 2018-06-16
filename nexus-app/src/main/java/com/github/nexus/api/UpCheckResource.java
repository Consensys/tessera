package com.github.nexus.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/upcheck")
public class UpCheckResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpCheckResource.class);

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
        LOGGER.info("GET upcheck");

        return UPCHECK_RESPONSE;
    }
}
