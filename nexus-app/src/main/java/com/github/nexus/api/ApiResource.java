package com.github.nexus.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

@Path("/api")
public class ApiResource {

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
    public Response api(@Context Request request) throws IOException {

        final List<Variant> variants = Variant.mediaTypes(MediaType.APPLICATION_JSON_TYPE,
                MediaType.TEXT_HTML_TYPE).build();

        final Variant variant = request.selectVariant(variants);

        final URL url;
        if (variant.getMediaType() == MediaType.APPLICATION_JSON_TYPE) {
            url = getClass().getResource("/swagger.json");
        } else if (variant.getMediaType() == MediaType.TEXT_HTML_TYPE) {
            url = getClass().getResource("/swagger.html");
        } else {
            return Response.status(Status.BAD_REQUEST).build();
        }

        return Response.ok(url.openStream(), variant.getMediaType())
                    .build();

    }

}
