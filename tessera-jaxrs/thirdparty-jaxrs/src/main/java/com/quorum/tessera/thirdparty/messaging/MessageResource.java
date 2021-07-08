package com.quorum.tessera.thirdparty.messaging;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.quorum.tessera.base64.Base64Codec;
import com.quorum.tessera.base64.DecodingException;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.messaging.Message;
import com.quorum.tessera.messaging.Messaging;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Objects;
import java.util.Optional;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Tag(name = "third-party")
@Path("/message")
public class MessageResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(MessageResource.class);

  private final Base64Codec base64Codec = Base64Codec.create();

  private final Messaging messaging;

  public MessageResource(Messaging messaging) {
    this.messaging = Objects.requireNonNull(messaging, "Messaging object must not be null");
  }

  @Operation(
      operationId = "sendMessage",
      summary = "/message/send",
      description = "Send a message",
      requestBody =
          @RequestBody(
              content =
                  @Content(
                      mediaType = APPLICATION_JSON,
                      schema = @Schema(implementation = SendMessageRequest.class))))
  @ApiResponse(
      responseCode = "200",
      description = "Sent message",
      content =
          @Content(
              mediaType = APPLICATION_JSON,
              schema = @Schema(implementation = SendMessageResponse.class)))
  @ApiResponse(responseCode = "400", description = "Bad request")
  @ApiResponse(responseCode = "404", description = "Message recipient not found")
  @POST
  @Path("/send")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public Response send(final SendMessageRequest request) {

    System.out.println("MessagingResource#sendMessage");

    // Decode the properties of the request
    byte[] from = null, to = null, data = null;
    try {
      from = Optional.ofNullable(request.getFrom()).map(base64Codec::decode).orElse(null);
      to = Optional.ofNullable(request.getTo()).map(base64Codec::decode).orElse(null);
      data = Optional.ofNullable(request.getData()).map(base64Codec::decode).orElse(null);
    } catch (DecodingException dex) {
      LOGGER.warn("Exception whilst decoding property of {}", request, dex);
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
    if (from == null || to == null) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }

    // Parse the public keys
    PublicKey sender = null, recipient = null;
    try {
      sender = PublicKey.from(from);
      recipient = PublicKey.from(to);
    } catch (Exception ex) {
      LOGGER.warn("Exception whilst parsing public key in {}", request, ex);
      return Response.status(Response.Status.BAD_REQUEST).build();
    }

    // Wrap it all up and send it on
    final Message message = new Message(sender, recipient, data);
    try {
      String messageId = messaging.send(message);

      SendMessageResponse response = new SendMessageResponse();
      response.setMessageId(messageId);

      return Response.status(Response.Status.OK).entity(response).build();
    } catch (Exception ex) {
      LOGGER.warn("Exception whilst processing send message request: {}", request, ex);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Operation(
      operationId = "listMessages",
      summary = "/message/list",
      description = "List received messages")
  @ApiResponse(
      responseCode = "200",
      description = "List of message identifiers",
      content =
          @Content(
              mediaType = APPLICATION_JSON,
              schema = @Schema(implementation = MessageListResponse.class)))
  @GET
  @Path("/list")
  @Produces(APPLICATION_JSON)
  public Response list() {

    try {
      MessageListResponse response = new MessageListResponse();
      response.setMessageIds(messaging.received());
      return Response.status(Response.Status.OK).entity(response).build();
    } catch (Exception ex) {
      LOGGER.warn("Exception whilst retrieving received messages", ex);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Operation(
      operationId = "getMessage",
      summary = "/message/get",
      description = "Retrieve a received message")
  @ApiResponse(
      responseCode = "200",
      description = "A received message",
      content =
          @Content(
              mediaType = APPLICATION_JSON,
              schema = @Schema(implementation = GetMessageResponse.class)))
  @GET
  @Path("/{messageId}")
  @Produces(APPLICATION_JSON)
  public Response get(@Parameter(required = true) @PathParam("messageId") String messageId) {

    LOGGER.info("Fetching {}", messageId);
    try {
      Message message = messaging.read(messageId);

      GetMessageResponse response = new GetMessageResponse();
      response.setFrom(message.getSender().encodeToBase64());
      response.setContent(base64Codec.encodeToString(message.getData()));
      return Response.status(Response.Status.OK).entity(response).build();
    } catch (Exception ex) {
      LOGGER.warn("Exception whilst retrieving received messages", ex);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Operation(
      operationId = "deleteMessage",
      summary = "/message/delete",
      description = "Delete a received message")
  @ApiResponse(responseCode = "204", description = "Message was deleted")
  @DELETE
  @Path("/{messageId}")
  @Produces(APPLICATION_JSON)
  public Response delete(@Parameter(required = true) @PathParam("messageId") String messageId) {

    try {
      messaging.remove(messageId);
      return Response.status(Response.Status.NO_CONTENT).build();
    } catch (Exception ex) {
      LOGGER.warn("Exception whilst removing received message with id {}", messageId, ex);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }
}
