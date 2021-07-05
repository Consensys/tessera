package com.quorum.tessera.p2p;

import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.messaging.Inbox;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Tag(name = "peer-to-peer")
@Path("/message")
public class MessageResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(MessageResource.class);

  private final Inbox inbox;

  public MessageResource(Inbox inbox) {
    this.inbox = inbox;
  }

  @Operation(
      summary = "/message/push",
      operationId = "pushMessage",
      description = "store encoded message to the server's database")
  @ApiResponse(
      responseCode = "201",
      description = "hash of encoded message",
      content =
          @Content(
              mediaType = TEXT_PLAIN,
              schema =
                  @Schema(
                      description = "hash of encrypted message",
                      type = "string",
                      format = "base64")))
  @PUT
  @Path("/push")
  @Consumes(APPLICATION_OCTET_STREAM)
  public Response push(@Schema(description = "encoded message") final byte[] message) {

    LOGGER.info(
        "Received inbound message {}", java.util.Base64.getEncoder().encodeToString(message));

    try {
      MessageHash messageHash = inbox.put(message);
      LOGGER.info("Stored message with identifier {}", messageHash);
      return Response.status(Response.Status.CREATED).entity(messageHash.toString()).build();
    } catch (Exception ex) {
      LOGGER.warn("Caught exception whilst storing message", ex);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }
}
