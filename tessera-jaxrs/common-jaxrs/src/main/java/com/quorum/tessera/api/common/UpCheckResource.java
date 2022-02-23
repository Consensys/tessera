package com.quorum.tessera.api.common;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Provides endpoints about the health status of this node */
@Tags({@Tag(name = "quorum-to-tessera"), @Tag(name = "peer-to-peer"), @Tag(name = "third-party")})
@Path("/upcheck")
public class UpCheckResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(UpCheckResource.class);

  private static final String UPCHECK_RESPONSE_IS_UP = "I'm up!";

  /**
   * Called to check if the application is running and responsive. Gives no details about the health
   * of the application other than it is up.
   *
   * @return a string stating the application is running
   */
  @Operation(
      summary = "/upcheck",
      operationId = "upcheck",
      description = "simple operation to check the server is up")
  @ApiResponse(
      responseCode = "200",
      description = "upcheck response",
      content =
          @Content(
              mediaType = MediaType.TEXT_PLAIN,
              schema = @Schema(type = "string"),
              examples = {
                @ExampleObject(name = UPCHECK_RESPONSE_IS_UP, value = UPCHECK_RESPONSE_IS_UP)
              }))
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public Response upCheck() {
    LOGGER.info("GET upcheck");
    return Response.ok(UPCHECK_RESPONSE_IS_UP).build();
  }
}
