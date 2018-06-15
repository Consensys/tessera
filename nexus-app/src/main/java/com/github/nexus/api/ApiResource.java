package com.github.nexus.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Variant;

@Path("/api")
public class ApiResource {

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
    public Response api(@Context Request request) throws IOException {

        final List<Variant> varients = Variant.mediaTypes(MediaType.APPLICATION_JSON_TYPE,
                MediaType.TEXT_HTML_TYPE).build();

        final Variant varient = request.selectVariant(varients);

        final URL url;
        if (varient.getMediaType() == MediaType.APPLICATION_JSON_TYPE) {
            url = getClass().getResource("/swagger.json");
        } else if (varient.getMediaType() == MediaType.TEXT_HTML_TYPE) {
            url = getClass().getResource("/swagger.html");
        } else {
            return Response.status(Status.BAD_REQUEST).build();
        }

        try (InputStream in = url.openStream()) {

            String data = Stream.of(in)
                    .map(InputStreamReader::new)
                    .map(BufferedReader::new)
                    .flatMap(BufferedReader::lines)
                    .collect(Collectors.joining());

            
            return Response.ok(data, varient.getMediaType())
                    
                    .build();

        }

    }

}
