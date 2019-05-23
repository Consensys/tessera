package com.quorum.tessera.test.ssl.sample;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/")
public class SampleResource {

    @Path("sample")
    @GET
    public String test() {
        return "TEST";
    }

}
