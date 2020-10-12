package com.quorum.tessera.api.common;

import com.quorum.tessera.api.StoreRawRequest;
import com.quorum.tessera.api.StoreRawResponse;
import com.quorum.tessera.core.api.ServiceFactory;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.transaction.TransactionManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.Objects;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/** Provides endpoints for dealing with raw transactions */
@Tags({@Tag(name = "quorum-to-tessera"), @Tag(name = "third-party")})
@Path("/")
public class RawTransactionResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(RawTransactionResource.class);

    public static final String ENDPOINT_STORE_RAW = "storeraw";

    private final TransactionManager transactionManager;

    public RawTransactionResource() {
        this(ServiceFactory.create().transactionManager());
    }

    public RawTransactionResource(final TransactionManager transactionManager) {
        this.transactionManager = Objects.requireNonNull(transactionManager);
    }

    @Operation(
            summary = "/storeraw",
            operationId = "encryptAndStore",
            description = "encrypts a payload and stores result in the \"raw\" database")
    @ApiResponse(
            responseCode = "200",
            description = "hash of encrypted payload",
            content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = StoreRawResponse.class)))
    @ApiResponse(responseCode = "404", description = "'from' key in request body not found")
    @POST
    @Path(ENDPOINT_STORE_RAW)
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response store(
            @RequestBody(required = true, content = @Content(schema = @Schema(implementation = StoreRawRequest.class)))
                    @NotNull
                    @Valid
                    final StoreRawRequest request) {

        PublicKey sender =
                request.getFrom()
                        .filter(Objects::nonNull)
                        .map(PublicKey::from)
                        .orElseGet(transactionManager::defaultPublicKey);

        com.quorum.tessera.transaction.StoreRawRequest storeRawRequest =
                com.quorum.tessera.transaction.StoreRawRequest.Builder.create()
                        .withSender(sender)
                        .withPayload(request.getPayload())
                        .build();

        final com.quorum.tessera.transaction.StoreRawResponse response = transactionManager.store(storeRawRequest);
        StoreRawResponse storeRawResponse = new StoreRawResponse();
        storeRawResponse.setKey(response.getHash().getHashBytes());
        return Response.ok().type(APPLICATION_JSON).entity(storeRawResponse).build();
    }
}
