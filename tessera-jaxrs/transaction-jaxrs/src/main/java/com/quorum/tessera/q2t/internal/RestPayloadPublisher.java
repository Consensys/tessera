package com.quorum.tessera.q2t.internal;

import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.EncodedPayloadCodec;
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
import com.quorum.tessera.version.MandatoryRecipientsVersion;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestPayloadPublisher implements PayloadPublisher {

  private static final Logger LOGGER = LoggerFactory.getLogger(RestPayloadPublisher.class);

  private final Client client;

  private final Discovery discovery;

  RestPayloadPublisher(Client client, Discovery discovery) {
    this.client = Objects.requireNonNull(client);
    this.discovery = Objects.requireNonNull(discovery);
  }

  @Override
  public void publishPayload(EncodedPayload payload, PublicKey recipientKey) {

    final NodeInfo remoteNodeInfo = discovery.getRemoteNodeInfo(recipientKey);
    final Set<String> supportedApiVersions = remoteNodeInfo.supportedApiVersions();
    final EncodedPayloadCodec preferredCodec =
        EncodedPayloadCodec.getPreferredCodec(supportedApiVersions);
    final PayloadEncoder payloadEncoder = PayloadEncoder.create(preferredCodec);

    if (PrivacyMode.STANDARD_PRIVATE != payload.getPrivacyMode()
        && !supportedApiVersions.contains(EnhancedPrivacyVersion.API_VERSION_2)) {
      throw new EnhancedPrivacyNotSupportedException(
          "Transactions with enhanced privacy is not currently supported on recipient "
              + recipientKey.encodeToBase64());
    }

    if (PrivacyMode.MANDATORY_RECIPIENTS == payload.getPrivacyMode()
        && !supportedApiVersions.contains(MandatoryRecipientsVersion.API_VERSION_4)) {
      throw new MandatoryRecipientsNotSupportedException(
          "Transactions with mandatory recipients are not currently supported on recipient "
              + recipientKey.encodeToBase64());
    }

    final String targetUrl = remoteNodeInfo.getUrl();
    LOGGER.info("Publishing message to {}", targetUrl);

    final byte[] encoded = payloadEncoder.encode(payload);

    try (Response response =
        client
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
