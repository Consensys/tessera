package com.quorum.tessera.q2t;

import com.quorum.tessera.api.*;
import com.quorum.tessera.enclave.PrivacyGroup;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.privacygroup.PrivacyGroupManager;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.json.Json;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.security.SecureRandom;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Tag(name = "quorum-to-tessera")
@Path("/")
public class PrivacyGroupResource {

    private final PrivacyGroupManager privacyGroupManager;


    public PrivacyGroupResource(PrivacyGroupManager privacyGroupManager) {
        this.privacyGroupManager = privacyGroupManager;
    }

    @POST
    @Path("createPrivacyGroup")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response createPrivacyGroup(@NotNull final PrivacyGroupRequest request) {

        final PublicKey from =
                Optional.ofNullable(request.getFrom())
                        .map(Base64.getDecoder()::decode)
                        .map(PublicKey::from)
                        .orElseGet(privacyGroupManager::defaultPublicKey);

        final List<PublicKey> members =
                Arrays.stream(request.getAddresses())
                        .map(Base64.getDecoder()::decode)
                        .map(PublicKey::from)
                        .collect(Collectors.toList());

        final byte[] randomSeed =
                Optional.ofNullable(request.getSeed()).map(Base64.getDecoder()::decode).orElseGet(generateRandomSeed);

        final PrivacyGroup created =
                privacyGroupManager.createPrivacyGroup(
                        request.getName(), request.getDescription(), from, members, randomSeed);

        return Response.status(Response.Status.OK).entity(toResponseObject(created)).build();
    }

    @POST
    @Path("findPrivacyGroup")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response findPrivacyGroup(@NotNull final PrivacyGroupSearchRequest searchRequest) {

        final List<PublicKey> members =
                Stream.of(searchRequest.getAddresses())
                        .map(Base64.getDecoder()::decode)
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

        final PrivacyGroup.Id privacyGroupId = PrivacyGroup.Id.fromBase64String(retrieveRequest.getPrivacyGroupId());

        final PrivacyGroup privacyGroup = privacyGroupManager.retrievePrivacyGroup(privacyGroupId);

        return Response.ok().entity(toResponseObject(privacyGroup)).build();
    }

    @POST
    @Path("deletePrivacyGroup")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response deletePrivacyGroup(@NotNull final PrivacyGroupDeleteRequest request) {

        final PublicKey from =
                Optional.ofNullable(request.getFrom())
                        .map(Base64.getDecoder()::decode)
                        .map(PublicKey::from)
                        .orElseGet(privacyGroupManager::defaultPublicKey);

        final PrivacyGroup.Id privacyGroupId = PrivacyGroup.Id.fromBase64String(request.getPrivacyGroupId());

        final PrivacyGroup privacyGroup = privacyGroupManager.deletePrivacyGroup(from, privacyGroupId);

        // Have to output in this format to match what is expected from Besu

        final String output = Optional.of(privacyGroup)
            .map(PrivacyGroup::getId)
            .map(PrivacyGroup.Id::getBytes)
            .map(Base64.getEncoder()::encodeToString)
            .map(s -> {
                return Json.createArrayBuilder().add(s).build();
            })
            .map(j -> j.getJsonString(0))
            .map(Objects::toString).get();

        return Response.ok().entity(output).build();
    }

    PrivacyGroupResponse toResponseObject(final PrivacyGroup privacyGroup) {
        return new PrivacyGroupResponse(
                privacyGroup.getId().getBase64(),
                privacyGroup.getName(),
                privacyGroup.getDescription(),
                privacyGroup.getType().name(),
                privacyGroup.getMembers().stream().map(PublicKey::encodeToBase64).toArray(String[]::new));
    }

    private final Supplier<byte[]> generateRandomSeed =
            () -> {
                final SecureRandom random = new SecureRandom();
                byte[] generated = new byte[20];
                random.nextBytes(generated);
                return generated;
            };
}
