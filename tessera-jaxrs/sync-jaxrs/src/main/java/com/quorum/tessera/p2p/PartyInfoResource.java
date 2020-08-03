package com.quorum.tessera.p2p;

import com.quorum.tessera.partyinfo.PartyInfoParser;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.model.Recipient;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.encryption.PublicKey;

import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;

/** Defines endpoints for requesting node discovery (partyinfo) information */
@Api
@Path("/partyinfo")
public class PartyInfoResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoResource.class);

    private final PartyInfoParser partyInfoParser;

    private final PartyInfoService partyInfoService;

    private final Client restClient;

    private final Enclave enclave;

    private final PayloadEncoder payloadEncoder;

    private final boolean enableKeyValidation;

    public PartyInfoResource(
            final PartyInfoService partyInfoService,
            final PartyInfoParser partyInfoParser,
            final Client restClient,
            final Enclave enclave,
            final PayloadEncoder payloadEncoder,
            final boolean enableKeyValidation) {
        this.partyInfoService = requireNonNull(partyInfoService, "partyInfoService must not be null");
        this.partyInfoParser = requireNonNull(partyInfoParser, "partyInfoParser must not be null");
        this.restClient = requireNonNull(restClient);
        this.enclave = requireNonNull(enclave);
        this.payloadEncoder = requireNonNull(payloadEncoder);
        this.enableKeyValidation = enableKeyValidation;
    }

    public PartyInfoResource(
            final PartyInfoService partyInfoService,
            final PartyInfoParser partyInfoParser,
            final Client restClient,
            final Enclave enclave,
            final boolean enableKeyValidation) {
        this(partyInfoService, partyInfoParser, restClient, enclave, PayloadEncoder.create(), enableKeyValidation);
    }

    /**
     * Update the local partyinfo store with the encoded partyinfo included in the request.
     *
     * @param payload The encoded partyinfo information pushed by the caller
     * @return an empty 200 OK Response if the local node is using remote key validation, else a 200 OK Response wrapping an encoded partyinfo containing only the local node's URL
     */
    @POST
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @ApiOperation(value = "Request public key/url of other nodes")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Empty response if node is using remote key validation, else an encoded partyinfo containing only the local node's URL", response = byte[].class),
        @ApiResponse(code = 500, message = "If node is using remote key validation, indicates validation failed")
    })
    public Response partyInfo(@ApiParam(required = true) final byte[] payload) {

        final PartyInfo partyInfo = partyInfoParser.from(payload);

        LOGGER.debug("Received PartyInfo from {}", partyInfo.getUrl());

        if (!enableKeyValidation) {
            LOGGER.debug("Key validation not enabled, passing PartyInfo through");
            partyInfoService.updatePartyInfo(partyInfo);

            // create an empty party info object with our URL to send back
            // this is used by older versions (before 0.10.0), but we don't want to give any info back
            final PartyInfo emptyInfo = new PartyInfo(partyInfoService.getPartyInfo().getUrl(), emptySet(), emptySet());
            final byte[] returnData = partyInfoParser.to(emptyInfo);
            return Response.ok(returnData).build();
        }

        final PublicKey localPublicKey = enclave.defaultPublicKey();
        final String partyInfoSender = partyInfo.getUrl();

        final Predicate<Recipient> isValidRecipient =
                r -> {
                    try {
                        LOGGER.debug("Validating key {} for peer {}", r.getKey(), r.getUrl());

                        final String dataToEncrypt = UUID.randomUUID().toString();
                        final EncodedPayload encodedPayload =
                                enclave.encryptPayload(dataToEncrypt.getBytes(), localPublicKey, Arrays.asList(r.getKey()));

                        final byte[] encodedPayloadBytes = payloadEncoder.encode(encodedPayload);

                        try (Response response =
                                restClient
                                        .target(r.getUrl())
                                        .path("partyinfo")
                                        .path("validate")
                                        .request()
                                        .post(Entity.entity(encodedPayloadBytes, MediaType.APPLICATION_OCTET_STREAM))) {

                            LOGGER.debug("Response code {} from peer {}", response.getStatus(), r.getUrl());

                            final String responseData = response.readEntity(String.class);

                            final boolean isValid = Objects.equals(responseData, dataToEncrypt);
                            if (!isValid) {
                                LOGGER.warn("Validation of key {} for peer {} failed.  Key and peer will not be added to local partyinfo.", r.getKey(), r.getUrl());
                                LOGGER.debug("Response from {} was {}", r.getUrl(), responseData);
                            }

                            return isValid;
                        }
                        // Assume any and all exceptions to mean invalid. enclave bubbles up nacl array out of
                        // bounds when calculating shared key from invalid data
                    } catch (Exception ex) {
                        LOGGER.debug(null, ex);
                        return false;
                    }
                };

        final Predicate<Recipient> isSender = r -> r.getUrl().equalsIgnoreCase(partyInfoSender);

        // Validate caller and treat no valid certs as security issue.
        final Set<Recipient> validatedSendersKeys =
                partyInfo.getRecipients().stream()
                        .filter(isSender.and(isValidRecipient))
                        .collect(Collectors.toSet());

        LOGGER.debug("Validated keys for peer {}: {}", partyInfoSender, validatedSendersKeys);
        if (validatedSendersKeys.isEmpty()) {
            throw new SecurityException("No validated keys found for peer " + partyInfoSender);
        }

        final PartyInfo reducedPartyInfo = new PartyInfo(partyInfoSender, validatedSendersKeys, partyInfo.getParties());
        partyInfoService.updatePartyInfo(reducedPartyInfo);

        return Response.ok().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Fetch network/peer information")
    @ApiResponses({@ApiResponse(code = 200, message = "Peer/Network information", response = PartyInfo.class)})
    public Response getPartyInfo() {

        final PartyInfo current = this.partyInfoService.getPartyInfo();

        // TODO: remove the filter when URIs don't need to end with a /
        final JsonArrayBuilder peersBuilder = Json.createArrayBuilder();
        current.getParties().stream()
                .filter(p -> p.getUrl().endsWith("/"))
                .map(
                        party -> {
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
        current.getRecipients().stream()
                .map(
                        recipient ->
                                Json.createObjectBuilder()
                                        .add("key", recipient.getKey().encodeToBase64())
                                        .add("url", recipient.getUrl())
                                        .build())
                .forEach(recipientBuilder::add);

        final String output =
                Json.createObjectBuilder()
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
    @ApiOperation(value = "Processes the validation request data and returns the result.")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Validation request data successfully processed and returned", response = String.class),
        @ApiResponse(code = 400, message = "Validation request data is not a valid UUID")
    })
    public Response validate(byte[] payloadData) {
        final EncodedPayload payload = payloadEncoder.decode(payloadData);

        final PublicKey mykey = payload.getRecipientKeys().iterator().next();

        final byte[] result = enclave.unencryptTransaction(payload, mykey);
        final String resultStr = new String(result);

        if (!isUUID(resultStr)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return Response.ok(new String(result)).build();
    }

    private boolean isUUID(String s) {
        try {
            UUID.fromString(s);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }
}
