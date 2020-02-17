package com.quorum.tessera.thirdparty;

import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.encryption.PublicKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Set;

@Api
@Path("/keys")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class KeyResource {


    @GET
    @ApiOperation(value = "Fetch local public keys managed by the enclave")
    @ApiResponses({@ApiResponse(code = 200, message = "Managed public keys")})
    public Response getPublicKeys() {

        RuntimeContext runtimeContext = RuntimeContext.getInstance();


        Set<PublicKey> publicKeys = runtimeContext.getPublicKeys();

        final JsonArrayBuilder keyBuilder = Json.createArrayBuilder();

        publicKeys.stream()
                .map(key -> Json.createObjectBuilder().add("key", key.encodeToBase64()).build())
                .forEach(keyBuilder::add);

        final String output = Json.createObjectBuilder().add("keys", keyBuilder.build()).build().toString();

        return Response.status(Response.Status.OK).entity(output).build();
    }
}
