package com.quorum.tessera.p2p;

import static jakarta.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;

import com.quorum.tessera.privacygroup.PrivacyGroupManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Tag(name = "peer-to-peer")
@Path("/")
public class PrivacyGroupResource {

  private PrivacyGroupManager privacyGroupManager;

  public PrivacyGroupResource(PrivacyGroupManager privacyGroupManager) {
    this.privacyGroupManager = privacyGroupManager;
  }

  @Operation(
      summary = "/pushPrivacyGroup",
      operationId = "pushPrivacyGroup",
      description = "store privacy group's encoded data")
  @ApiResponse(responseCode = "200", description = "privacy group payload stored successfully")
  @POST
  @Path("pushPrivacyGroup")
  @Consumes(APPLICATION_OCTET_STREAM)
  public Response storePrivacyGroup(@NotNull final byte[] privacyGroupData) {

    privacyGroupManager.storePrivacyGroup(privacyGroupData);

    return Response.status(Response.Status.OK).build();
  }
}
