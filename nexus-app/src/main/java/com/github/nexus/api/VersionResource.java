package com.github.nexus.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/version")
public class VersionResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(VersionResource.class);

    private static final String VERSION = "No version defined yet!";

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getVersion() {
        LOGGER.info("GET version");

        return VERSION;
    }
}
