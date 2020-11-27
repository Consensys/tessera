package com.quorum.tessera.p2p;

import com.quorum.tessera.privacygroup.PrivacyGroupManager;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;

@Path("/")
public class PrivacyGroupResource {

    private PrivacyGroupManager privacyGroupManager;

    public PrivacyGroupResource(PrivacyGroupManager privacyGroupManager) {
        this.privacyGroupManager = privacyGroupManager;
    }

    @POST
    @Path("pushPrivacyGroup")
    @Consumes(APPLICATION_OCTET_STREAM)
    public Response storePrivacyGroup(@NotNull final byte[] privacyGroupData) {

        privacyGroupManager.storePrivacyGroup(privacyGroupData);

        return Response.status(Response.Status.CREATED).build();
    }
}
