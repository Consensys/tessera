package com.quorum.tessera.api.common;

import com.quorum.tessera.transaction.TransactionManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import java.util.Objects;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Provides endpoints about the health status of this node */
@Tags({@Tag(name = "quorum-to-tessera"), @Tag(name = "peer-to-peer"), @Tag(name = "third-party")})
@Path("/upcheck")
public class UpCheckResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(UpCheckResource.class);

  private static final String UPCHECK_RESPONSE_IS_UP = "I'm up!";
  private static final String UPCHECK_RESPONSE_DB = "Database unavailable";

  private final TransactionManager transactionManager;

  public UpCheckResource(final TransactionManager transactionManager) {
    this.transactionManager = Objects.requireNonNull(transactionManager);
  }

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
                @ExampleObject(name = UPCHECK_RESPONSE_IS_UP, value = UPCHECK_RESPONSE_IS_UP),
                @ExampleObject(name = UPCHECK_RESPONSE_DB, value = UPCHECK_RESPONSE_DB)
              }))
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public Response upCheck() {
    LOGGER.info("GET upcheck");

    if (!transactionManager.upcheck()) {
      return Response.ok(UPCHECK_RESPONSE_DB).build();
    }

    return Response.ok(UPCHECK_RESPONSE_IS_UP).build();
  }
}
