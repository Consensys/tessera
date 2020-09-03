package com.quorum.tessera.q2t;

import com.quorum.tessera.api.*;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.enclave.RecipientBox;
import com.quorum.tessera.enclave.TxHash;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.transaction.EncodedPayloadManager;
import com.quorum.tessera.transaction.TransactionManager;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Api
@Path("/encodedpayload")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class EncodedPayloadResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(EncodedPayloadResource.class);

    private final Base64.Decoder base64Decoder = Base64.getDecoder();

    private final EncodedPayloadManager encodedPayloadManager;

    private final TransactionManager transactionManager;

    public EncodedPayloadResource(final EncodedPayloadManager encodedPayloadManager,
                                  final TransactionManager transactionManager) {
        this.encodedPayloadManager = Objects.requireNonNull(encodedPayloadManager);
        this.transactionManager = Objects.requireNonNull(transactionManager);
    }

    @POST
    @Path("create")
    @ApiOperation(value = "Send private transaction payload")
    @ApiResponses({
        @ApiResponse(code = 200, response = SendResponse.class, message = "Send response"),
    })
    public Response createEncodedPayload(
        @ApiParam(value = "Request details containing the data to encrypt and recipients to encrypt for", name = "sendRequest", required = true)
        @NotNull @Valid final SendRequest sendRequest
    ) {
        LOGGER.info("Encrypting message without saving to database");

        final PublicKey sender =
            Optional.ofNullable(sendRequest.getFrom())
                .map(base64Decoder::decode)
                .map(PublicKey::from)
                .orElseGet(transactionManager::defaultPublicKey);

        final List<PublicKey> recipientList =
            Stream.of(sendRequest)
                .filter(sr -> Objects.nonNull(sr.getTo()))
                .flatMap(s -> Stream.of(s.getTo()))
                .map(base64Decoder::decode)
                .map(PublicKey::from)
                .collect(Collectors.toList());

        final Set<MessageHash> affectedTransactions =
            Stream.ofNullable(sendRequest.getAffectedContractTransactions())
                .flatMap(Arrays::stream)
                .map(Base64.getDecoder()::decode)
                .map(MessageHash::new)
                .collect(Collectors.toSet());

        final byte[] execHash =
            Optional.ofNullable(sendRequest.getExecHash()).map(String::getBytes).orElse(new byte[0]);

        final PrivacyMode privacyMode = PrivacyMode.fromFlag(sendRequest.getPrivacyFlag());

        final com.quorum.tessera.transaction.SendRequest request =
            com.quorum.tessera.transaction.SendRequest.Builder.create()
                .withRecipients(recipientList)
                .withSender(sender)
                .withPayload(sendRequest.getPayload())
                .withExecHash(execHash)
                .withPrivacyMode(privacyMode)
                .withAffectedContractTransactions(affectedTransactions)
                .build();

        LOGGER.debug("Sender key: {}", sender.encodeToBase64());
        LOGGER.debug("Recipient list: {}", recipientList);

        final EncodedPayload encodedPayload = encodedPayloadManager.create(request);

        final Map<String, String> affectedContractTransactionMap =
            encodedPayload.getAffectedContractTransactions().entrySet()
                .stream()
                .collect(Collectors.toMap(e -> e.getKey().encodeToBase64(), e -> Base64.getEncoder().encodeToString(e.getValue().getData())));
        final PayloadEncryptResponse response = new PayloadEncryptResponse();
        response.setSenderKey(encodedPayload.getSenderKey().getKeyBytes());
        response.setCipherText(encodedPayload.getCipherText());
        response.setCipherTextNonce(encodedPayload.getCipherTextNonce().getNonceBytes());
        response.setRecipientBoxes(encodedPayload.getRecipientBoxes().stream().map(RecipientBox::getData).collect(Collectors.toList()));
        response.setRecipientNonce(encodedPayload.getRecipientNonce().getNonceBytes());
        response.setRecipientKeys(encodedPayload.getRecipientKeys().stream().map(PublicKey::getKeyBytes).collect(Collectors.toList()));
        response.setPrivacyMode(encodedPayload.getPrivacyMode().getPrivacyFlag());
        response.setAffectedContractTransactions(affectedContractTransactionMap);
        response.setExecHash(encodedPayload.getExecHash());

        return Response.ok(response).type(APPLICATION_JSON).build();
    }

    @POST
    @Path("decrypt")
    @ApiOperation(value = "Returns decrypted payload back to Quorum")
    @ApiResponses({@ApiResponse(code = 200, response = ReceiveResponse.class, message = "Receive Response object")})
    public Response receive(
        @ApiParam("Encrypted payload that this node should attempt to decrypt")
        @Valid @NotNull final PayloadDecryptRequest request
    ) {
        LOGGER.info("Decrypting custom transaction");

        final Base64.Decoder decoder = Base64.getDecoder();
        final Map<TxHash, byte[]> affectedTxns = request.getAffectedContractTransactions()
            .entrySet()
            .stream()
            .collect(Collectors.toMap(e -> TxHash.from(decoder.decode(e.getKey())), e -> decoder.decode(e.getValue())));

        final EncodedPayload requestAsPayload = EncodedPayload.Builder.create()
            .withSenderKey(PublicKey.from(request.getSenderKey()))
            .withCipherText(request.getCipherText())
            .withCipherTextNonce(request.getCipherTextNonce())
            .withRecipientBoxes(request.getRecipientBoxes())
            .withRecipientNonce(request.getRecipientNonce())
            .withRecipientKeys(request.getRecipientKeys().stream().map(PublicKey::from).collect(Collectors.toList()))
            .withPrivacyFlag(request.getPrivacyMode())
            .withAffectedContractTransactions(affectedTxns)
            .withExecHash(request.getExecHash())
            .build();

        final com.quorum.tessera.transaction.ReceiveResponse response
            = encodedPayloadManager.decrypt(requestAsPayload, null);

        final ReceiveResponse receiveResponse = new ReceiveResponse();
        receiveResponse.setPrivacyFlag(response.getPrivacyMode().getPrivacyFlag());
        receiveResponse.setPayload(response.getUnencryptedTransactionData());
        receiveResponse.setAffectedContractTransactions(
            response.getAffectedTransactions()
                .stream()
                .map(MessageHash::getHashBytes)
                .map(Base64.getEncoder()::encodeToString)
                .toArray(String[]::new)
        );

        Optional.ofNullable(response.getExecHash()).map(String::new).ifPresent(receiveResponse::setExecHash);

        return Response.ok(receiveResponse).build();
    }
}
