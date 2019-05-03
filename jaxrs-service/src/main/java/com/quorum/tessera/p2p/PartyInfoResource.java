package com.quorum.tessera.p2p;

import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.node.PartyInfoParser;
import com.quorum.tessera.node.PartyInfoService;
import com.quorum.tessera.node.model.PartyInfo;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.Objects;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.node.model.Recipient;
import java.util.Arrays;
import static java.util.Objects.requireNonNull;
import java.util.UUID;
import javax.ws.rs.client.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines endpoints for requesting node discovery (partyinfo) information
 */
@Path("/partyinfo")
public class PartyInfoResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoResource.class);

    private final PartyInfoParser partyInfoParser;

    private final PartyInfoService partyInfoService;

    private final Client restClient;

    private final Enclave enclave;

    private final PayloadEncoder payloadEncoder;

    public PartyInfoResource(
        final PartyInfoService partyInfoService,
        final PartyInfoParser partyInfoParser,
        Client restClient,
        Enclave enclave,
        PayloadEncoder payloadEncoder) {
        this.partyInfoService = requireNonNull(partyInfoService, "partyInfoService must not be null");
        this.partyInfoParser = requireNonNull(partyInfoParser, "partyInfoParser must not be null");
        this.restClient = requireNonNull(restClient);
        this.enclave = requireNonNull(enclave);
        this.payloadEncoder = payloadEncoder;
    }

    public PartyInfoResource(
        final PartyInfoService partyInfoService,
        final PartyInfoParser partyInfoParser,
        Client restClient,
        Enclave enclave) {

        this(partyInfoService, partyInfoParser, restClient, enclave, PayloadEncoder.create());
    }

    /**
     * Allows node information to be retrieved in a specific encoded form
     * including other node URLS and public key to URL mappings
     *
     * @param payload The encoded node information from the requester
     * @return the merged node information from this node, which may contain new
     * information
     */
    @POST
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @ApiOperation(value = "Request public key/url of other nodes", produces = "public keylist/url")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Encoded PartyInfo Data", response = byte[].class)})
    public Response partyInfo(@ApiParam(required = true) final byte[] payload) {

        final PartyInfo partyInfo = partyInfoParser.from(payload);

        //Start validation stuff
        PublicKey sender = enclave.defaultPublicKey();

        String url = partyInfo.getUrl();
        PublicKey key = partyInfo.getRecipients().stream()
            .filter(r -> url.equalsIgnoreCase(r.getUrl()))
            .reduce((l, r) -> {
                throw new IllegalStateException("Found multiple recipients for url " + url);
            }).map(Recipient::getKey)
            .get();

        final String dataToEncrypt = UUID.randomUUID().toString();

        final EncodedPayload encodedPayload = enclave.encryptPayload(dataToEncrypt.getBytes(), sender, Arrays.asList(key));

        byte[] encodedPayloadData = payloadEncoder.encode(encodedPayload);

        String unencodedValidationData = restClient.target(partyInfo.getUrl())
            .path("partyinfo")
            .path("validate")
            .request()
            .post(Entity.entity(encodedPayloadData, MediaType.APPLICATION_OCTET_STREAM))
            .readEntity(String.class);

        if (!Objects.equals(unencodedValidationData, dataToEncrypt)) {
            LOGGER.trace("{} does not equal {}", dataToEncrypt, unencodedValidationData);
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
    @ApiResponses({
        @ApiResponse(code = 200, message = "Peer/Network information", response = PartyInfo.class)})
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
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.TEXT_PLAIN)
    public Response validate(byte[] payloadData) {

        EncodedPayload payload = payloadEncoder.decode(payloadData);

        PublicKey mykey = enclave.defaultPublicKey();

        byte[] result = enclave.unencryptTransaction(payload, mykey);

        return Response.ok(new String(result)).build();

    }

}
