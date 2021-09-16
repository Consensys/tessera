package com.quorum.tessera.q2t.internal;

import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.privacygroup.exception.PrivacyGroupNotSupportedException;
import com.quorum.tessera.privacygroup.exception.PrivacyGroupPublishException;
import com.quorum.tessera.privacygroup.publish.PrivacyGroupPublisher;
import com.quorum.tessera.transaction.publish.NodeOfflineException;
import com.quorum.tessera.version.PrivacyGroupVersion;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestPrivacyGroupPublisher implements PrivacyGroupPublisher {

  private static final Logger LOGGER = LoggerFactory.getLogger(RestPrivacyGroupPublisher.class);

  private final Discovery discovery;

  private final Client restClient;

  public RestPrivacyGroupPublisher(Discovery discovery, Client restClient) {
    this.discovery = discovery;
    this.restClient = restClient;
  }

  @Override
  public void publishPrivacyGroup(byte[] data, PublicKey recipientKey) {

    final NodeInfo remoteNodeInfo = discovery.getRemoteNodeInfo(recipientKey);

    if (!remoteNodeInfo.supportedApiVersions().contains(PrivacyGroupVersion.API_VERSION_3)) {
      throw new PrivacyGroupNotSupportedException(
          "Transactions with privacy group is not currently supported on recipient "
              + recipientKey.encodeToBase64());
    }

    final String targetUrl = remoteNodeInfo.getUrl();

    LOGGER.info("Publishing privacy group to {}", targetUrl);

    try (Response response =
        restClient
            .target(targetUrl)
            .path("/pushPrivacyGroup")
            .request()
            .post(Entity.entity(data, MediaType.APPLICATION_OCTET_STREAM_TYPE))) {

      if (Response.Status.OK.getStatusCode() != response.getStatus()) {
        throw new PrivacyGroupPublishException(
            "Unable to push privacy group to recipient url " + targetUrl);
      }
      LOGGER.info("Published privacy group to {}", targetUrl);
    } catch (ProcessingException ex) {
      LOGGER.debug("", ex);
      throw new NodeOfflineException(URI.create(targetUrl));
    }
  }
}
