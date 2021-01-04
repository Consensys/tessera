package com.quorum.tessera.q2t;

import com.quorum.tessera.enclave.PrivacyGroup;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.privacygroup.PrivacyGroupManager;
import com.quorum.tessera.util.Base64Codec;
import com.quorum.tessera.api.PrivacyGroupResponse;
import com.quorum.tessera.api.PrivacyGroupRequest;
import com.quorum.tessera.api.PrivacyGroupRetrieveRequest;
import com.quorum.tessera.api.PrivacyGroupSearchRequest;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Tag(name = "quorum-to-tessera")
@Path("/")
public class PrivacyGroupResource {

    private PrivacyGroupManager privacyGroupManager;

    private Base64Codec base64Codec;

    public PrivacyGroupResource(PrivacyGroupManager privacyGroupManager) {
        this.privacyGroupManager = privacyGroupManager;
        this.base64Codec = Base64Codec.create();
    }

    @POST
    @Path("createPrivacyGroup")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response createPrivacyGroup(@NotNull final PrivacyGroupRequest request) {

        final List<PublicKey> members =
                Arrays.stream(request.getAddresses())
                        .map(base64Codec::decode)
                        .map(PublicKey::from)
                        .collect(Collectors.toList());

        final byte[] randomSeed =
                Optional.ofNullable(request.getSeed()).map(base64Codec::decode).orElseGet(generateRandomSeed);

        final PrivacyGroup created =
                privacyGroupManager.createPrivacyGroup(
                        request.getName(), request.getDescription(), members, randomSeed);

        return Response.status(Response.Status.OK).entity(toResponseObject(created)).build();
    }

    @POST
    @Path("findPrivacyGroup")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response findPrivacyGroup(@NotNull final PrivacyGroupSearchRequest searchRequest) {

        final List<PublicKey> members =
                Stream.of(searchRequest.getAddresses())
                        .map(base64Codec::decode)
                        .map(PublicKey::from)
                        .collect(Collectors.toList());

        final List<PrivacyGroup> privacyGroups = privacyGroupManager.findPrivacyGroup(members);

        final PrivacyGroupResponse[] results =
                privacyGroups.stream().map(this::toResponseObject).toArray(PrivacyGroupResponse[]::new);

        return Response.status(Response.Status.OK).type(APPLICATION_JSON).entity(results).build();
    }

    @POST
    @Path("retrievePrivacyGroup")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response retrievePrivacyGroup(@NotNull final PrivacyGroupRetrieveRequest retrieveRequest) {

        final PublicKey privacyGroupId = PublicKey.from(base64Codec.decode(retrieveRequest.getPrivacyGroupId()));

        final PrivacyGroup privacyGroup = privacyGroupManager.retrievePrivacyGroup(privacyGroupId);

        return Response.ok().entity(toResponseObject(privacyGroup)).build();
    }

    PrivacyGroupResponse toResponseObject(final PrivacyGroup privacyGroup) {
        return new PrivacyGroupResponse(
                privacyGroup.getPrivacyGroupId().encodeToBase64(),
                privacyGroup.getName(),
                privacyGroup.getDescription(),
                privacyGroup.getType().name(),
                privacyGroup.getMembers().stream().map(PublicKey::encodeToBase64).toArray(String[]::new));
    }

    private Supplier<byte[]> generateRandomSeed =
            () -> {
                final SecureRandom random = new SecureRandom();
                byte[] generated = new byte[20];
                random.nextBytes(generated);
                return generated;
            };
}
