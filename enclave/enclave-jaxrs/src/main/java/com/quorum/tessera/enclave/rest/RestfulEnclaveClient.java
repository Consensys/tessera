package com.quorum.tessera.enclave.rest;

import com.quorum.tessera.enclave.EnclaveClient;
import com.quorum.tessera.enclave.EnclaveNotAvailableException;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.enclave.RawTransaction;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.nacl.Nonce;
import com.quorum.tessera.service.Service;

import javax.json.JsonArray;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestfulEnclaveClient implements EnclaveClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestfulEnclaveClient.class);

    private final Client client;

    private final URI uri;

    private final ExecutorService executorService;

    public RestfulEnclaveClient(Client client, URI uri) {
        this(client, uri, Executors.newSingleThreadExecutor());
    }

    public RestfulEnclaveClient(Client client, URI uri, ExecutorService executorService) {
        this.client = Objects.requireNonNull(client);
        this.uri = Objects.requireNonNull(uri);
        this.executorService = executorService;
    }

    @Override
    public PublicKey defaultPublicKey() {

        return ClientCallback.execute(
                () -> {
                    Response response = client.target(uri).path("default").request().get();

                    validateResponseIsOk(response);

                    byte[] data = response.readEntity(byte[].class);

                    return PublicKey.from(data);
                });
    }

    @Override
    public Set<PublicKey> getForwardingKeys() {
        return ClientCallback.execute(
                () -> {
                    Response response = client.target(uri).path("forwarding").request().get();

                    validateResponseIsOk(response);

                    JsonArray results = response.readEntity(JsonArray.class);

                    return IntStream.range(0, results.size())
                            .mapToObj(i -> results.getString(i))
                            .map(s -> Base64.getDecoder().decode(s))
                            .map(PublicKey::from)
                            .collect(Collectors.toSet());
                });
    }

    @Override
    public Set<PublicKey> getPublicKeys() {
        return ClientCallback.execute(
                () -> {
                    Response response = client.target(uri).path("public").request().get();

                    validateResponseIsOk(response);

                    JsonArray results = response.readEntity(JsonArray.class);

                    return IntStream.range(0, results.size())
                            .mapToObj(i -> results.getString(i))
                            .map(s -> Base64.getDecoder().decode(s))
                            .map(PublicKey::from)
                            .collect(Collectors.toSet());
                });
    }

    @Override
    public EncodedPayload encryptPayload(
            byte[] message, PublicKey senderPublicKey, List<PublicKey> recipientPublicKeys) {

        return ClientCallback.execute(
                () -> {
                    EnclavePayload enclavePayload = new EnclavePayload();
                    enclavePayload.setData(message);
                    enclavePayload.setSenderKey(senderPublicKey.getKeyBytes());
                    enclavePayload.setRecipientPublicKeys(
                            recipientPublicKeys.stream().map(PublicKey::getKeyBytes).collect(Collectors.toList()));

                    Response response = client.target(uri).path("encrypt").request().post(Entity.json(enclavePayload));

                    validateResponseIsOk(response);

                    byte[] result = response.readEntity(byte[].class);

                    return PayloadEncoder.create().decode(result);
                });
    }

    @Override
    public EncodedPayload encryptPayload(RawTransaction rawTransaction, List<PublicKey> recipientPublicKeys) {

        return ClientCallback.execute(
                () -> {
                    EnclaveRawPayload enclaveRawPayload = new EnclaveRawPayload();
                    enclaveRawPayload.setNonce(rawTransaction.getNonce().getNonceBytes());
                    enclaveRawPayload.setFrom(rawTransaction.getFrom().getKeyBytes());
                    enclaveRawPayload.setRecipientPublicKeys(
                            recipientPublicKeys.stream().map(PublicKey::getKeyBytes).collect(Collectors.toList()));
                    enclaveRawPayload.setEncryptedPayload(rawTransaction.getEncryptedPayload());
                    enclaveRawPayload.setEncryptedKey(rawTransaction.getEncryptedKey());

                    Response response =
                            client.target(uri)
                                    .path("encrypt")
                                    .path("raw")
                                    .request()
                                    .post(Entity.json(enclaveRawPayload));

                    validateResponseIsOk(response);

                    byte[] body = response.readEntity(byte[].class);

                    return PayloadEncoder.create().decode(body);
                });
    }

    @Override
    public RawTransaction encryptRawPayload(byte[] message, PublicKey sender) {

        return ClientCallback.execute(
                () -> {
                    EnclavePayload enclavePayload = new EnclavePayload();
                    enclavePayload.setData(message);
                    enclavePayload.setSenderKey(sender.getKeyBytes());

                    Response response =
                            client.target(uri)
                                    .path("encrypt")
                                    .path("toraw")
                                    .request()
                                    .post(Entity.json(enclavePayload));

                    validateResponseIsOk(response);

                    EnclaveRawPayload enclaveRawPayload = response.readEntity(EnclaveRawPayload.class);

                    byte[] encryptedPayload = enclaveRawPayload.getEncryptedPayload();
                    byte[] encryptedKey = enclaveRawPayload.getEncryptedKey();
                    Nonce nonce = new Nonce(enclaveRawPayload.getNonce());
                    PublicKey senderKey = PublicKey.from(enclaveRawPayload.getFrom());
                    return new RawTransaction(encryptedPayload, encryptedKey, nonce, senderKey);
                });
    }

    @Override
    public byte[] unencryptTransaction(EncodedPayload payload, PublicKey providedKey) {

        return ClientCallback.execute(
                () -> {
                    EnclaveUnencryptPayload dto = new EnclaveUnencryptPayload();

                    byte[] body = PayloadEncoder.create().encode(payload);

                    dto.setData(body);

                    if (providedKey != null) {
                        dto.setProvidedKey(providedKey.getKeyBytes());
                    }
                    Response response = client.target(uri).path("unencrypt").request().post(Entity.json(dto));

                    validateResponseIsOk(response);

                    return response.readEntity(byte[].class);
                });
    }

    @Override
    public byte[] createNewRecipientBox(final EncodedPayload payload, final PublicKey recipientKey) {

        return ClientCallback.execute(
                () -> {
                    final byte[] body = PayloadEncoder.create().encode(payload);

                    final EnclaveUnencryptPayload dto = new EnclaveUnencryptPayload();
                    dto.setData(body);
                    dto.setProvidedKey(recipientKey.getKeyBytes());

                    final Response response = client.target(uri).path("addRecipient").request().post(Entity.json(dto));

                    validateResponseIsOk(response);

                    return response.readEntity(byte[].class);
                });
    }

    /**
     * In the case of a stateless client there is no start/stop all the run status logic is handled in the status
     * command itself
     *
     * @return Status
     */
    @Override
    public Service.Status status() {

        Future<Service.Status> outcome =
                executorService.submit(
                        () -> {
                            Response response = client.target(uri).path("ping").request().get();

                            if (response.getStatus() == 200) {
                                return Service.Status.STARTED;
                            }
                            return Service.Status.STOPPED;
                        });

        try {
            // TODO: 2 seconds is arguably a long time
            return outcome.get(2, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            LOGGER.trace(null, ex);
            return Service.Status.STOPPED;
        }
    }

    private static void validateResponseIsOk(Response response) {
        if (response.getStatus() != 200) {
            Response.StatusType statusInfo = response.getStatusInfo();
            String message =
                    String.format(
                            "Remote enclave instance threw an error %d  %s",
                            statusInfo.getStatusCode(), statusInfo.getReasonPhrase());

            throw new EnclaveNotAvailableException(message);
        }
    }
}
