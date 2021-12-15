package com.quorum.tessera.enclave.rest;

import com.quorum.tessera.enclave.*;
import com.quorum.tessera.encryption.Nonce;
import com.quorum.tessera.encryption.PublicKey;
import jakarta.json.JsonArray;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestfulEnclaveClient implements EnclaveClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(RestfulEnclaveClient.class);

  private final Client client;

  private final URI uri;

  private final ExecutorService executorService;

  private final PayloadEncoder payloadEncoder;

  public RestfulEnclaveClient(Client client, URI uri) {
    this(client, uri, Executors.newSingleThreadExecutor());
  }

  public RestfulEnclaveClient(Client client, URI uri, ExecutorService executorService) {
    this.client = Objects.requireNonNull(client);
    this.uri = Objects.requireNonNull(uri);
    this.payloadEncoder = PayloadEncoder.create(EncodedPayloadCodec.LEGACY);
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
              .mapToObj(results::getString)
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
              .mapToObj(results::getString)
              .map(s -> Base64.getDecoder().decode(s))
              .map(PublicKey::from)
              .collect(Collectors.toSet());
        });
  }

  @Override
  public EncodedPayload encryptPayload(
      final byte[] message,
      final PublicKey senderPublicKey,
      final List<PublicKey> recipientPublicKeys,
      final PrivacyMetadata privacyMetaData) {

    return ClientCallback.execute(
        () -> {
          EnclavePayload enclavePayload = new EnclavePayload();
          enclavePayload.setData(message);
          enclavePayload.setSenderKey(senderPublicKey.getKeyBytes());
          enclavePayload.setRecipientPublicKeys(
              recipientPublicKeys.stream()
                  .map(PublicKey::getKeyBytes)
                  .collect(Collectors.toList()));
          enclavePayload.setPrivacyMode(privacyMetaData.getPrivacyMode());
          enclavePayload.setAffectedContractTransactions(
              convertAffectedContractTransactions(
                  privacyMetaData.getAffectedContractTransactions()));
          enclavePayload.setExecHash(privacyMetaData.getExecHash());
          enclavePayload.setMandatoryRecipients(
              privacyMetaData.getMandatoryRecipients().stream()
                  .map(PublicKey::getKeyBytes)
                  .collect(Collectors.toList()));
          privacyMetaData
              .getPrivacyGroupId()
              .map(PrivacyGroup.Id::getBytes)
              .ifPresent(enclavePayload::setPrivacyGroupId);

          Response response =
              client.target(uri).path("encrypt").request().post(Entity.json(enclavePayload));

          validateResponseIsOk(response);

          byte[] result = response.readEntity(byte[].class);

          return payloadEncoder.decode(result);
        });
  }

  @Override
  public EncodedPayload encryptPayload(
      final RawTransaction rawTransaction,
      final List<PublicKey> recipientPublicKeys,
      final PrivacyMetadata privacyMetaData) {

    return ClientCallback.execute(
        () -> {
          EnclaveRawPayload enclaveRawPayload = new EnclaveRawPayload();
          enclaveRawPayload.setNonce(rawTransaction.getNonce().getNonceBytes());
          enclaveRawPayload.setFrom(rawTransaction.getFrom().getKeyBytes());
          enclaveRawPayload.setRecipientPublicKeys(
              recipientPublicKeys.stream()
                  .map(PublicKey::getKeyBytes)
                  .collect(Collectors.toList()));
          enclaveRawPayload.setEncryptedPayload(rawTransaction.getEncryptedPayload());
          enclaveRawPayload.setEncryptedKey(rawTransaction.getEncryptedKey());

          enclaveRawPayload.setPrivacyMode(privacyMetaData.getPrivacyMode());
          enclaveRawPayload.setExecHash(privacyMetaData.getExecHash());

          enclaveRawPayload.setMandatoryRecipients(
              privacyMetaData.getMandatoryRecipients().stream()
                  .map(PublicKey::getKeyBytes)
                  .collect(Collectors.toList()));

          enclaveRawPayload.setAffectedContractTransactions(
              convertAffectedContractTransactions(
                  privacyMetaData.getAffectedContractTransactions()));

          privacyMetaData
              .getPrivacyGroupId()
              .map(PrivacyGroup.Id::getBytes)
              .ifPresent(enclaveRawPayload::setPrivacyGroupId);

          Response response =
              client
                  .target(uri)
                  .path("encrypt")
                  .path("raw")
                  .request()
                  .post(Entity.json(enclaveRawPayload));

          validateResponseIsOk(response);

          byte[] body = response.readEntity(byte[].class);

          return payloadEncoder.decode(body);
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
              client
                  .target(uri)
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

          byte[] body = payloadEncoder.encode(payload);

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
  public byte[] unencryptRawPayload(RawTransaction payload) {

    return ClientCallback.execute(
        () -> {
          EnclaveRawPayload enclaveRawPayload = new EnclaveRawPayload();
          enclaveRawPayload.setEncryptedPayload(payload.getEncryptedPayload());
          enclaveRawPayload.setEncryptedKey(payload.getEncryptedKey());
          enclaveRawPayload.setNonce(payload.getNonce().getNonceBytes());
          enclaveRawPayload.setFrom(payload.getFrom().getKeyBytes());

          Response response =
              client
                  .target(uri)
                  .path("unencrypt")
                  .path("raw")
                  .request()
                  .post(Entity.json(enclaveRawPayload));

          return response.readEntity(byte[].class);
        });
  }

  @Override
  public byte[] createNewRecipientBox(final EncodedPayload payload, final PublicKey recipientKey) {

    return ClientCallback.execute(
        () -> {
          final byte[] body = payloadEncoder.encode(payload);

          final EnclaveUnencryptPayload dto = new EnclaveUnencryptPayload();
          dto.setData(body);
          dto.setProvidedKey(recipientKey.getKeyBytes());

          final Response response =
              client.target(uri).path("addRecipient").request().post(Entity.json(dto));

          validateResponseIsOk(response);

          return response.readEntity(byte[].class);
        });
  }

  @Override
  public Set<TxHash> findInvalidSecurityHashes(
      EncodedPayload encodedPayload, List<AffectedTransaction> affectedContractTransactions) {
    EnclaveFindInvalidSecurityHashesRequestPayload requestPayload =
        new EnclaveFindInvalidSecurityHashesRequestPayload();
    requestPayload.setEncodedPayload(this.payloadEncoder.encode(encodedPayload));
    requestPayload.setAffectedContractTransactions(
        convertAffectedContractTransactions(affectedContractTransactions));

    Response response =
        client
            .target(uri)
            .path("findinvalidsecurityhashes")
            .request()
            .post(Entity.json(requestPayload));

    EnclaveFindInvalidSecurityHashesResponsePayload responsePayload =
        response.readEntity(EnclaveFindInvalidSecurityHashesResponsePayload.class);

    return responsePayload.getInvalidSecurityHashes().stream()
        .map(TxHash::new)
        .collect(Collectors.toSet());
  }

  /**
   * In the case of a stateless client there is no start/stop all the run status logic is handled in
   * the status command itself
   *
   * @return Status
   */
  @Override
  public Status status() {

    Future<Status> outcome =
        executorService.submit(
            () -> {
              Response response = client.target(uri).path("ping").request().get();

              if (response.getStatus() == 200) {
                return Status.STARTED;
              }
              return Status.STOPPED;
            });

    try {
      // TODO: 2 seconds is arguably a long time
      return outcome.get(2, TimeUnit.SECONDS);
    } catch (InterruptedException | ExecutionException | TimeoutException ex) {
      LOGGER.trace(null, ex);
      return Status.STOPPED;
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

  private List<KeyValuePair> convertAffectedContractTransactions(
      List<AffectedTransaction> affectedContractTransactions) {
    return affectedContractTransactions.stream()
        .map(
            affectedTransaction ->
                new KeyValuePair(
                    affectedTransaction.getHash().getBytes(),
                    this.payloadEncoder.encode(affectedTransaction.getPayload())))
        .collect(Collectors.toList());
  }
}
