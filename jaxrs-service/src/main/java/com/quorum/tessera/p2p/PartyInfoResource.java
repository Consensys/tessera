package com.quorum.tessera.p2p;

import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.node.PartyInfoParser;
import com.quorum.tessera.node.PartyInfoService;
import com.quorum.tessera.node.model.PartyInfo;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.io.StringReader;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonObject;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import com.quorum.tessera.enclave.Enclave;
import static java.util.Objects.requireNonNull;
import java.util.UUID;
import javax.ws.rs.client.Entity;
/**
 * Defines endpoints for requesting node discovery (partyinfo) information
 */
@Path("/partyinfo")
public class PartyInfoResource {

    private final PartyInfoParser partyInfoParser;

    private final PartyInfoService partyInfoService;

    private Client restClient;
    
    private Enclave enclave;
    
    public PartyInfoResource(
        final PartyInfoService partyInfoService, 
        final PartyInfoParser partyInfoParser,
        Client restClient,
        Enclave enclave) {
        this.partyInfoService = requireNonNull(partyInfoService, "partyInfoService must not be null");
        this.partyInfoParser = requireNonNull(partyInfoParser, "partyInfoParser must not be null");
        this.restClient = requireNonNull(restClient);
        this.enclave = requireNonNull(enclave);
    }

    /**
     * Allows node information to be retrieved in a specific encoded form
     * including other node URLS and public key to URL mappings
     *
     * @param payload The encoded node information from the requester
     * @return the merged node information from this node, which may contain new information
     */
    @POST
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @ApiOperation(value = "Request public key/url of other nodes", produces = "public keylist/url")
    @ApiResponses({@ApiResponse(code = 200, message = "Encoded PartyInfo Data", response = byte[].class)})
    public Response partyInfo(@ApiParam(required = true) final byte[] payload) {

        final PartyInfo partyInfo = partyInfoParser.from(payload);

        //Start validatio stuff
        PublicKey myKey = partyInfo.getRecipients()
            .stream()
            .filter(r -> r.getUrl().equals(partyInfo.getUrl())).findAny().get().getKey();
        
        byte[] someData = UUID.randomUUID().toString().getBytes();
        
        String controlHash = Base64.getEncoder().encodeToString(enclave.encryptRawPayload(someData, myKey).getEncryptedPayload());
        
        String encodedData = Base64.getEncoder().encodeToString(someData);
            
        JsonObject validationRequest = Json.createObjectBuilder()
            .add("data", encodedData)
            .build();
            
        String validationHash = restClient.target(partyInfo.getUrl())
            .path("partyinfo")
            .path("validate")
            .request()
            .post(Entity.text(validationRequest.toString()))
            .readEntity(String.class);
        
        if(!Objects.equals(validationHash, controlHash)) {
            throw new SecurityException("Invalid node registration");
        }

        //End validation stuff

        final PartyInfo updatedPartyInfo = partyInfoService.updatePartyInfo(partyInfo);


        final byte[] encoded = partyInfoParser.to(updatedPartyInfo);

        final StreamingOutput streamingOutput = out -> out.write(encoded);

        return Response.status(Response.Status.OK).entity(streamingOutput).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Fetch network/peer information", produces = "public list of peers/publickey mappings")
    @ApiResponses({@ApiResponse(code = 200, message = "Peer/Network information", response = PartyInfo.class)})
    public Response getPartyInfo() {

        final PartyInfo current = this.partyInfoService.getPartyInfo();

        //TODO: remove the filter when URIs don't need to end with a /
        final JsonArrayBuilder peersBuilder = Json.createArrayBuilder();
        current.getParties()
            .stream()
            .filter(p -> p.getUrl().endsWith("/"))
            .map(party -> {
                final JsonObjectBuilder builder = Json.createObjectBuilder();
                builder.add("url", party.getUrl());
                if (party.getLastContacted() != null) {
                    builder.add("lastContact", party.getLastContacted().toString());
                } else {
                    builder.addNull("lastContact");
                }
                return builder.build();
            })
            .forEach(peersBuilder::add);

        final JsonArrayBuilder recipientBuilder = Json.createArrayBuilder();
        current.getRecipients()
            .stream()
            .map(recipient -> Json
                .createObjectBuilder()
                .add("key", recipient.getKey().encodeToBase64())
                .add("url", recipient.getUrl())
                .build()
            ).forEach(recipientBuilder::add);

        final String output = Json
            .createObjectBuilder()
            .add("url", current.getUrl())
            .add("peers", peersBuilder.build())
            .add("keys", recipientBuilder.build())
            .build()
            .toString();

        return Response.status(Response.Status.OK).entity(output).build();
    }

    @POST
    @Path("validate")
    public Response validate(String requeststr) {

        JsonObject request = Json.createReader(new StringReader(requeststr)).readObject();

        byte[] dataToEncrypt = Optional.of(request)
            .map(j -> j.getString("data"))
            .map(Base64.getDecoder()::decode).get();
        
        
        PublicKey sender = enclave.defaultPublicKey();
        
       byte[] result = enclave.encryptRawPayload(dataToEncrypt, sender).getEncryptedPayload();
       
       String encodedResult = Base64.getEncoder().encodeToString(result);
       
       return Response.ok(encodedResult,MediaType.TEXT_PLAIN).build();
  
    }
    
}
