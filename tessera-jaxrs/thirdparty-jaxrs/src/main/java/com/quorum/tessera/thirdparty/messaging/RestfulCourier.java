package com.quorum.tessera.thirdparty.messaging;

import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.encryption.KeyNotFoundException;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.messaging.Courier;
import com.quorum.tessera.messaging.CourierException;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.util.Base64Codec;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestfulCourier implements Courier {

  private static final Logger LOGGER = LoggerFactory.getLogger(RestfulCourier.class);

  private final Client client;
  private final Discovery discovery;
  private final PayloadEncoder payloadEncoder;
  private final Base64Codec base64Codec = Base64Codec.create();

  public RestfulCourier(Client client, Discovery discovery) {
    this(client, discovery, PayloadEncoder.create());
  }

  public RestfulCourier(Client client, Discovery discovery, PayloadEncoder payloadEncoder) {
    this.client = client;
    this.discovery = discovery;
    this.payloadEncoder = payloadEncoder;

    LOGGER.info("RestfulCourier instantiated.");
  }

  @Override
  public boolean isKnownRecipient(PublicKey publicKey) {

    final String url = getUrlForNode(publicKey);
    return ((url != null) && !url.isEmpty());
  }

  @Override
  public byte[] send(EncodedPayload message, PublicKey to) {

    String targetUrl = null;
    try {
      targetUrl = getUrlForNode(to);
    } catch (Exception ex) {
      LOGGER.warn("Exception whilst retrieving URL for {}", to, ex);
      throw new CourierException(String.format("Failed to retrieve exception for %s", to), ex);
    }
    if (targetUrl == null) {
      throw new CourierException(String.format("Failed to retrieve exception for %s", to));
    }

    final byte[] encoded = payloadEncoder.encode(message);
    try (Response response =
        client
            .target(targetUrl)
            .path("/message/push")
            .request()
            .put(Entity.entity(encoded, MediaType.APPLICATION_OCTET_STREAM_TYPE))) {

      if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
        throw new CourierException(String.format("Message not accepted by %s", targetUrl));
      }
      final String string = response.readEntity(String.class);
      LOGGER.info("Message sent with response: {}", string);
      return base64Codec.decode(string);
    } catch (ProcessingException pex) {
      LOGGER.warn("Exception whilst forwarding message to {}", targetUrl, pex);
      throw new CourierException(String.format("%s is offline", targetUrl), pex);
    }
  }

  String getUrlForNode(PublicKey publicKey) {

    // Look for an early out
    if (publicKey == null) {
      return null;
    }
    try {
      final NodeInfo nodeInfo = discovery.getRemoteNodeInfo(publicKey);
      final String url = (nodeInfo == null) ? null : nodeInfo.getUrl();
      return (url == null) ? null : url.trim();
    } catch (KeyNotFoundException ex) {
      LOGGER.warn("Unknown node {}", publicKey.encodeToBase64());
    }
    return null;
  }
}
