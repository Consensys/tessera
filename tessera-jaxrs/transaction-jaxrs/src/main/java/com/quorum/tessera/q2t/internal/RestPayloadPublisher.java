package com.quorum.tessera.q2t.internal;

import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.transaction.exception.EnhancedPrivacyNotSupportedException;
import com.quorum.tessera.transaction.exception.MandatoryRecipientsNotSupportedException;
import com.quorum.tessera.transaction.publish.NodeOfflineException;
import com.quorum.tessera.transaction.publish.PayloadPublisher;
import com.quorum.tessera.transaction.publish.PublishPayloadException;
import com.quorum.tessera.version.EnhancedPrivacyVersion;
import java.net.URI;
import java.util.Objects;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.quorum.tessera.version.MandatoryRecipientsVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestPayloadPublisher implements PayloadPublisher {

  private static final Logger LOGGER = LoggerFactory.getLogger(RestPayloadPublisher.class);

  private final Client restclient;

  private final PayloadEncoder payloadEncoder;

  private final Discovery discovery;

  RestPayloadPublisher(Client restclient, PayloadEncoder payloadEncoder, Discovery discovery) {
    this.restclient = Objects.requireNonNull(restclient);
    this.payloadEncoder = Objects.requireNonNull(payloadEncoder);
    this.discovery = Objects.requireNonNull(discovery);
  }

  @Override
  public void publishPayload(EncodedPayload payload, PublicKey recipientKey) {

    final NodeInfo remoteNodeInfo = discovery.getRemoteNodeInfo(recipientKey);

    if (PrivacyMode.STANDARD_PRIVATE != payload.getPrivacyMode()
        && !remoteNodeInfo.supportedApiVersions().contains(EnhancedPrivacyVersion.API_VERSION_2)) {
      throw new EnhancedPrivacyNotSupportedException(
          "Transactions with enhanced privacy is not currently supported on recipient "
              + recipientKey.encodeToBase64());
    }

    if (PrivacyMode.MANDATORY_RECIPIENTS == payload.getPrivacyMode()
      && !remoteNodeInfo.supportedApiVersions().contains(MandatoryRecipientsVersion.API_VERSION_4)) {
      throw new MandatoryRecipientsNotSupportedException(
        "Transactions with mandatory recipients are not currently supported on recipient "
          + recipientKey.encodeToBase64());
    }

    final String targetUrl = remoteNodeInfo.getUrl();
    LOGGER.info("Publishing message to {}", targetUrl);

    final byte[] encoded = payloadEncoder.encode(payload);

    try (Response response =
        restclient
            .target(targetUrl)
            .path("/push")
            .request()
            .post(Entity.entity(encoded, MediaType.APPLICATION_OCTET_STREAM_TYPE))) {

      if (Response.Status.OK.getStatusCode() != response.getStatus()
          && Response.Status.CREATED.getStatusCode() != response.getStatus()) {
        throw new PublishPayloadException("Unable to push payload to recipient url " + targetUrl);
      }

      LOGGER.info("Published to {}", targetUrl);
    } catch (ProcessingException ex) {
      LOGGER.debug("", ex);
      throw new NodeOfflineException(URI.create(targetUrl));
    }
  }
}
