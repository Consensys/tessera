package com.quorum.tessera.p2p;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;

import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.discovery.NodeUri;
import com.quorum.tessera.enclave.*;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.p2p.model.GetPartyInfoResponse;
import com.quorum.tessera.p2p.partyinfo.PartyInfoParser;
import com.quorum.tessera.p2p.partyinfo.PartyStore;
import com.quorum.tessera.partyinfo.model.NodeInfoUtil;
import com.quorum.tessera.partyinfo.model.Party;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.model.Recipient;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.shared.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Defines endpoints for requesting node discovery (partyinfo) information */
@Tag(name = "peer-to-peer")
@Path("/partyinfo")
public class PartyInfoResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoResource.class);

  private final PartyInfoParser partyInfoParser;

  private final Discovery discovery;

  private final Client restClient;

  private final Enclave enclave;

  private final PayloadEncoder payloadEncoder;

  private final boolean enableKeyValidation;

  private final PartyStore partyStore;

  public PartyInfoResource(
      final Discovery discovery,
      final PartyInfoParser partyInfoParser,
      final Client restClient,
      final Enclave enclave,
      final PayloadEncoder payloadEncoder,
      final boolean enableKeyValidation,
      final PartyStore partyStore) {
    this.discovery = requireNonNull(discovery, "discovery must not be null");
    this.partyInfoParser = requireNonNull(partyInfoParser, "partyInfoParser must not be null");
    this.restClient = requireNonNull(restClient);
    this.enclave = requireNonNull(enclave);
    this.payloadEncoder = requireNonNull(payloadEncoder);
    this.enableKeyValidation = enableKeyValidation;
    this.partyStore = requireNonNull(partyStore);
  }

  public PartyInfoResource(
      final Discovery discovery,
      final PartyInfoParser partyInfoParser,
      final Client restClient,
      final Enclave enclave,
      final boolean enableKeyValidation) {
    this(
        discovery,
        partyInfoParser,
        restClient,
        enclave,
        PayloadEncoder.create(EncodedPayloadCodec.LEGACY),
        enableKeyValidation,
        PartyStore.getInstance());
  }

  /**
   * Update the local partyinfo store with the encoded partyinfo included in the request.
   *
   * @param payload The encoded partyinfo information pushed by the caller
   * @return an empty 200 OK Response if the local node is using remote key validation; a 200 OK
   *     Response wrapping an encoded partyinfo that contains only the local node's URL if not using
   *     remote key validation; a 500 Internal Server Error if remote key validation fails
   */
  @Operation(
      summary = "/partyinfo",
      operationId = "broadcastPartyInfo",
      description = "broadcast partyinfo information to server")
  @ApiResponse(
      responseCode = "200",
      description = "server successfully updated its party info",
      content =
          @Content(
              array =
                  @ArraySchema(
                      schema =
                          @Schema(
                              description =
                                  "empty if server is using remote key validation, else is encoded partyinfo object containing only the server's URL",
                              type = "string",
                              format = "byte"))))
  @ApiResponse(
      responseCode = "500",
      description = "Validation failed (if server is using remote key validation)")
  @POST
  @Consumes(MediaType.APPLICATION_OCTET_STREAM)
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response partyInfo(
      @RequestBody(required = true, description = "partyinfo object") final byte[] payload,
      @HeaderParam(Constants.API_VERSION_HEADER)
          @Parameter(
              description = "client's supported API versions",
              array = @ArraySchema(schema = @Schema(type = "string")))
          final List<String> headers) {

    final PartyInfo partyInfo = partyInfoParser.from(payload);
    final Set<String> versions =
        Optional.ofNullable(headers).orElse(emptyList()).stream()
            .filter(Objects::nonNull)
            .flatMap(v -> Arrays.stream(v.split(",")))
            .collect(Collectors.toSet());

    final NodeInfo nodeInfo = NodeInfoUtil.from(partyInfo, versions);

    LOGGER.debug("Received PartyInfo from {}", partyInfo.getUrl());

    if (!enableKeyValidation) {
      LOGGER.debug("Key validation not enabled, passing PartyInfo through");

      discovery.onUpdate(nodeInfo);
      partyInfo.getParties().stream()
          .map(Party::getUrl)
          .map(NodeUri::create)
          .map(NodeUri::asURI)
          .forEach(partyStore::store);

      // create an empty party info object with our URL to send back
      // this is used by older versions (before 0.10.0), but we don't want to give any info back
      final PartyInfo emptyInfo =
          new PartyInfo(discovery.getCurrent().getUrl(), emptySet(), emptySet());
      final byte[] returnData = partyInfoParser.to(emptyInfo);
      return Response.ok(returnData).build();
    }

    final PublicKey localPublicKey = enclave.defaultPublicKey();

    final Predicate<Recipient> isValidRecipient =
        r -> {
          try {
            LOGGER.debug("Validating key {} for peer {}", r.getKey(), r.getUrl());

            final String dataToEncrypt = UUID.randomUUID().toString();
            final EncodedPayload encodedPayload =
                enclave.encryptPayload(
                    dataToEncrypt.getBytes(),
                    localPublicKey,
                    List.of(r.getKey()),
                    PrivacyMetadata.Builder.forStandardPrivate().build());

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
                LOGGER.warn(
                    "Validation of key {} for peer {} failed.  Key and peer will not be added to local partyinfo.",
                    r.getKey(),
                    r.getUrl());
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

    final String partyInfoSender = partyInfo.getUrl();
    final Predicate<Recipient> isSender =
        r -> NodeUri.create(r.getUrl()).equals(NodeUri.create(partyInfoSender));

    // Validate caller and treat no valid certs as security issue.
    final Set<com.quorum.tessera.partyinfo.node.Recipient> validatedSendersKeys =
        partyInfo.getRecipients().stream()
            .filter(isSender.and(isValidRecipient))
            .map(r -> com.quorum.tessera.partyinfo.node.Recipient.of(r.getKey(), r.getUrl()))
            .collect(Collectors.toSet());

    LOGGER.debug("Validated keys for peer {}: {}", partyInfoSender, validatedSendersKeys);
    if (validatedSendersKeys.isEmpty()) {
      throw new SecurityException("No validated keys found for peer " + partyInfoSender);
    }

    // End validation stuff
    final NodeInfo reducedNodeInfo =
        NodeInfo.Builder.create()
            .withUrl(partyInfoSender)
            .withSupportedApiVersions(versions)
            .withRecipients(validatedSendersKeys)
            .build();

    discovery.onUpdate(reducedNodeInfo);

    partyInfo.getParties().stream()
        .map(Party::getUrl)
        .map(NodeUri::create)
        .map(NodeUri::asURI)
        .forEach(partyStore::store);

    return Response.ok().build();
  }

  @Operation(summary = "/partyinfo", description = "fetch network/peer information")
  @ApiResponse(
      responseCode = "200",
      description = "server's partyinfo data",
      content = @Content(schema = @Schema(implementation = GetPartyInfoResponse.class)))
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getPartyInfo() {

    final NodeInfo current = this.discovery.getCurrent();

    final JsonArrayBuilder peersBuilder = Json.createArrayBuilder();

    partyStore.getParties().stream()
        .map(party -> Json.createObjectBuilder().add("url", party.toString()).build())
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

    LOGGER.debug("Sending json {} from {}", output, current);

    return Response.status(Response.Status.OK).entity(output).build();
  }

  @Operation(
      summary = "/partyinfo/validate",
      operationId = "validateParty",
      description = "decrypt a UUID payload (used to validate ownership of an asymmetric key pair)")
  @ApiResponse(
      responseCode = "200",
      description = "successfully decrypted payload",
      content = @Content(schema = @Schema(description = "decrypted UUID", type = "string")))
  @ApiResponse(responseCode = "400", description = "decrypted payload is not a valid UUID")
  @POST
  @Path("validate")
  @Consumes(MediaType.APPLICATION_OCTET_STREAM)
  @Produces(MediaType.TEXT_PLAIN)
  public Response validate(@Schema(description = "encrypted UUID") byte[] payloadData) {
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
