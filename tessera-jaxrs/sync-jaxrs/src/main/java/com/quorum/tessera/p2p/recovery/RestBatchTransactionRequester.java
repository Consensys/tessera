package com.quorum.tessera.p2p.recovery;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.p2p.resend.ResendRequest;
import com.quorum.tessera.recovery.resend.BatchTransactionRequester;
import java.util.Base64;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestBatchTransactionRequester implements BatchTransactionRequester {

  private static final Logger LOGGER = LoggerFactory.getLogger(RestBatchTransactionRequester.class);

  private final Enclave enclave;

  private final RecoveryClient client;

  private final int batchSize;

  public RestBatchTransactionRequester(
      final Enclave enclave, final RecoveryClient client, int batchSize) {
    this.enclave = Objects.requireNonNull(enclave);
    this.client = Objects.requireNonNull(client);
    this.batchSize = batchSize;
  }

  @Override
  public boolean requestAllTransactionsFromNode(final String uri) {

    LOGGER.info("Requesting transactions get resent for {}", uri);

    return this.enclave.getPublicKeys().stream()
        .map(this::createRequestAllEntity)
        .allMatch(req -> this.makeRequest(uri, req) >= 0);
  }

  @Override
  public boolean requestAllTransactionsFromLegacyNode(String uri) {

    LOGGER.info("Requesting transactions get resent for legacy node {}", uri);

    return this.enclave.getPublicKeys().stream()
        .map(this::createLegacyRequest)
        .allMatch(req -> this.makeLegacyRequest(uri, req));
  }

  /**
   * Will make the desired request until succeeds or max tries has been reached
   *
   * @param uri the URI to call
   * @param request the request object to send
   */
  private long makeRequest(final String uri, final ResendBatchRequest request) {
    LOGGER.debug("Requesting a batch resend for key {}", request.getPublicKey());

    ResendBatchResponse response = null;
    int numberOfTries = 0;

    do {

      try {

        response = client.makeBatchResendRequest(uri, request);
      } catch (final Exception ex) {
        LOGGER.debug(
            "Failed to make batch resend request to node {} for key {}",
            uri,
            request.getPublicKey());
      }

      numberOfTries++;

    } while ((null == response) && (numberOfTries < MAX_ATTEMPTS));

    return response != null ? response.getTotal() : -1;
  }

  /**
   * Creates the entity that should be sent to the target URL
   *
   * @param key the public key that transactions should be resent for
   * @return the request to be sent
   */
  private ResendBatchRequest createRequestAllEntity(final PublicKey key) {

    final ResendBatchRequest request = new ResendBatchRequest();
    String encoded = Base64.getEncoder().encodeToString(key.getKeyBytes());
    request.setPublicKey(encoded);
    request.setBatchSize(batchSize);

    return request;
  }

  /**
   * Will make the legacy resend request to legacy nodes that don't support the new recovery process
   *
   * @param uri the URI to call
   * @param request the request object to send
   */
  private boolean makeLegacyRequest(final String uri, final ResendRequest request) {
    LOGGER.debug("Requesting a resend to {} for key {}", uri, request.getPublicKey());

    try {
      return client.makeResendRequest(uri, request);
    } catch (final Exception ex) {
      LOGGER.warn(
          "Failed to make resend request to node {} for key {}, due to {}",
          uri,
          request.getPublicKey(),
          ex.getMessage());
      return false;
    }
  }

  /**
   * Creates the legacy entity that should be sent to the legacy target URL
   *
   * @param key the public key that transactions should be resent for
   * @return the request to be sent
   */
  private ResendRequest createLegacyRequest(final PublicKey key) {

    final ResendRequest request = new ResendRequest();
    final String encoded = key.encodeToBase64();
    request.setPublicKey(encoded);
    request.setType("ALL");

    return request;
  }
}
