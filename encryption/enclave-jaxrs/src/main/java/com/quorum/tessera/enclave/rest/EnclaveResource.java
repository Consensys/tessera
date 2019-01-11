package com.quorum.tessera.enclave.rest;

import com.quorum.tessera.encryption.Enclave;
import com.quorum.tessera.encryption.EncodedPayloadWithRecipients;
import com.quorum.tessera.encryption.PayloadEncoder;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.encryption.RawTransaction;
import com.quorum.tessera.nacl.Nonce;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.json.Json;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

@Path("/")
public class EnclaveResource {

    private final Enclave enclave;

    private final PayloadEncoder payloadEncoder = PayloadEncoder.create();

    public EnclaveResource(Enclave enclave) {
        this.enclave = Objects.requireNonNull(enclave);
    }

    @GET
    @Path("ping")
    public String ping() {
        return LocalDateTime.now().toString();
    }

    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @GET
    @Path("default")
    public Response defaultPublicKey() {
        final StreamingOutput streamingOutput = out -> out.write(enclave.defaultPublicKey().getKeyBytes());
        return Response.ok(streamingOutput)
                .build();
    }

    @GET
    @Produces("application/json")
    @Path("forwarding")
    public Response getForwardingKeys() {

        List<String> body = enclave.getForwardingKeys()
                .stream()
                .map(PublicKey::encodeToBase64)
                .collect(Collectors.toList());

        return Response.ok(Json.createArrayBuilder(body).build().toString(), MediaType.APPLICATION_JSON_TYPE)
                .build();

    }

    @GET
    @Produces("application/json")
    @Path("public")
    public Response getPublicKeys() {

        List<String> body = enclave.getPublicKeys()
                .stream()
                .map(PublicKey::encodeToBase64)
                .collect(Collectors.toList());

        return Response.ok(Json.createArrayBuilder(body).build().toString(), MediaType.APPLICATION_JSON_TYPE)
                .build();
    }

    @POST
    @Path("encrypt")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response encryptPayload(EnclavePayload payload) {

        PublicKey senderKey = PublicKey.from(payload.getSenderKey());

        List<PublicKey> recipientPublicKeys = payload.getRecipientPublicKeys()
                .stream()
                .map(PublicKey::from)
                .collect(Collectors.toList());

        EncodedPayloadWithRecipients outcome = enclave.encryptPayload(payload.getData(), senderKey, recipientPublicKeys);

        byte[] response = payloadEncoder.encode(outcome);
        final StreamingOutput streamingOutput = out -> out.write(response);
        return Response.ok(streamingOutput)
                .build();
    }

    @POST
    @Path("encrypt/raw")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response encryptPayload(EnclaveRawPayload enclaveRawPayload) {

        byte[] encryptedPayload = enclaveRawPayload.getEncryptedPayload();
        byte[] encryptedKey = enclaveRawPayload.getEncryptedKey();
        Nonce nonce = new Nonce(enclaveRawPayload.getNonce());
        PublicKey from = PublicKey.from(enclaveRawPayload.getFrom());

        List<PublicKey> recipientPublicKeys = enclaveRawPayload.getRecipientPublicKeys().stream()
                .map(PublicKey::from).collect(Collectors.toList());

        RawTransaction rawTransaction = new RawTransaction(encryptedPayload, encryptedKey, nonce, from);

        EncodedPayloadWithRecipients outcome = enclave.encryptPayload(rawTransaction, recipientPublicKeys);

        byte[] response = payloadEncoder.encode(outcome);
        final StreamingOutput streamingOutput = out -> out.write(response);
        return Response.ok(streamingOutput)
                .build();
    }

    @POST
    @Path("encrypt/toraw")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response encryptRawPayload(EnclavePayload payload) {

        RawTransaction rawTransaction = enclave.encryptRawPayload(payload.getData(), PublicKey.from(payload.getSenderKey()));

        EnclaveRawPayload enclaveRawPayload = new EnclaveRawPayload();
        enclaveRawPayload.setFrom(rawTransaction.getFrom().getKeyBytes());
        enclaveRawPayload.setNonce(rawTransaction.getNonce().getNonceBytes());
        enclaveRawPayload.setEncryptedPayload(rawTransaction.getEncryptedPayload());
        enclaveRawPayload.setEncryptedKey(rawTransaction.getEncryptedKey());

        return Response.ok(enclaveRawPayload).build();

    }

    @POST
    @Path("unencrypt")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response unencryptTransaction(EnclaveUnencryptPayload enclaveUnencryptPayload) {

        EncodedPayloadWithRecipients payloadWithRecipients
                = payloadEncoder.decodePayloadWithRecipients(enclaveUnencryptPayload.getData());
        PublicKey providedKey = PublicKey.from(enclaveUnencryptPayload.getProvidedKey());

        byte[] response = enclave.unencryptTransaction(payloadWithRecipients, providedKey);

        final StreamingOutput streamingOutput = out -> out.write(response);
        return Response.ok(streamingOutput).build();

    }

}
