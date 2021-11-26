package com.quorum.tessera.q2t.internal;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Fail.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.*;

import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.EncodedPayloadCodec;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.partyinfo.node.Recipient;
import com.quorum.tessera.transaction.exception.EnhancedPrivacyNotSupportedException;
import com.quorum.tessera.transaction.exception.MandatoryRecipientsNotSupportedException;
import com.quorum.tessera.transaction.publish.NodeOfflineException;
import com.quorum.tessera.transaction.publish.PublishPayloadException;
import com.quorum.tessera.version.EnhancedPrivacyVersion;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

public class RestPayloadPublisherTest {

  private Client client;

  private PayloadEncoder payloadEncoder;

  private final MockedStatic<PayloadEncoder> payloadEncoderFactoryFunction =
      mockStatic(PayloadEncoder.class);

  private Discovery discovery;

  private RestPayloadPublisher payloadPublisher;

  @Before
  public void beforeTest() {
    client = mock(Client.class);
    payloadEncoder = mock(PayloadEncoder.class);
    discovery = mock(Discovery.class);
    payloadPublisher = new RestPayloadPublisher(client, discovery);

    payloadEncoderFactoryFunction
        .when(() -> PayloadEncoder.create(any(EncodedPayloadCodec.class)))
        .thenReturn(payloadEncoder);
  }

  @After
  public void afterTest() {
    try {
      verifyNoMoreInteractions(client, payloadEncoder, discovery);
      payloadEncoderFactoryFunction.verifyNoMoreInteractions();
    } finally {
      payloadEncoderFactoryFunction.close();
    }
  }

  @Test
  public void publish() {
    final String targetUrl = "nodeUrl";
    final EncodedPayload encodedPayload = mock(EncodedPayload.class);
    final PublicKey publicKey = mock(PublicKey.class);

    for (Response.Status expectedResponseStatus : Response.Status.values()) {

      for (PrivacyMode privacyMode : PrivacyMode.values()) {
        when(encodedPayload.getPrivacyMode()).thenReturn(privacyMode);

        final NodeInfo nodeInfo = mock(NodeInfo.class);
        when(nodeInfo.supportedApiVersions())
            .thenReturn(Set.of(EnhancedPrivacyVersion.API_VERSION_2, "2.1", "3.0", "4.0"));
        when(nodeInfo.getUrl()).thenReturn(targetUrl);

        when(discovery.getRemoteNodeInfo(publicKey)).thenReturn(nodeInfo);

        final byte[] payloadData = "Payload".getBytes();
        when(payloadEncoder.encode(encodedPayload)).thenReturn(payloadData);

        WebTarget webTarget = mock(WebTarget.class);
        when(client.target(targetUrl)).thenReturn(webTarget);
        when(webTarget.path("/push")).thenReturn(webTarget);

        Invocation.Builder invocationBuilder = mock(Invocation.Builder.class);

        Response response = Response.status(expectedResponseStatus).build();
        when(invocationBuilder.post(
                Entity.entity(payloadData, MediaType.APPLICATION_OCTET_STREAM_TYPE)))
            .thenReturn(response);
        when(webTarget.request()).thenReturn(invocationBuilder);

        if (expectedResponseStatus == Response.Status.OK
            || expectedResponseStatus == Response.Status.CREATED) {
          payloadPublisher.publishPayload(encodedPayload, publicKey);
        } else {
          PublishPayloadException publishPayloadException =
              Assertions.catchThrowableOfType(
                  () -> payloadPublisher.publishPayload(encodedPayload, publicKey),
                  PublishPayloadException.class);
          assertThat(publishPayloadException)
              .hasMessage(String.format("Unable to push payload to recipient url %s", targetUrl));
        }
      }
    }

    int iterations = Response.Status.values().length * PrivacyMode.values().length;
    verify(client, times(iterations)).target(targetUrl);
    verify(discovery, times(iterations)).getRemoteNodeInfo(publicKey);
    verify(payloadEncoder, times(iterations)).encode(encodedPayload);
    payloadEncoderFactoryFunction.verify(
        times(iterations), () -> PayloadEncoder.create(any(EncodedPayloadCodec.class)));
  }

  @Test
  public void publishEnhancedTransactionsToNodesThatDoNotSupport() {

    Map<PrivacyMode, Set<String>> privacyModeAndVersions = new HashMap<>();
    privacyModeAndVersions.put(PrivacyMode.PARTY_PROTECTION, Set.of("v1"));
    privacyModeAndVersions.put(PrivacyMode.PRIVATE_STATE_VALIDATION, Set.of("v1"));

    for (Map.Entry<PrivacyMode, Set<String>> pair : privacyModeAndVersions.entrySet()) {
      String targetUrl = "http://someplace.com";

      EncodedPayload encodedPayload = mock(EncodedPayload.class);
      when(encodedPayload.getPrivacyMode()).thenReturn(pair.getKey());
      byte[] payloadData = "Some Data".getBytes();
      when(payloadEncoder.encode(encodedPayload)).thenReturn(payloadData);

      PublicKey recipientKey = mock(PublicKey.class);
      NodeInfo nodeInfo = mock(NodeInfo.class);
      when(nodeInfo.supportedApiVersions()).thenReturn(pair.getValue());
      Recipient recipient = mock(Recipient.class);
      when(recipient.getKey()).thenReturn(recipientKey);
      when(recipient.getUrl()).thenReturn(targetUrl);

      when(nodeInfo.getRecipients()).thenReturn(Set.of(recipient));
      when(discovery.getRemoteNodeInfo(recipientKey)).thenReturn(nodeInfo);

      EnhancedPrivacyNotSupportedException exception =
          catchThrowableOfType(
              () -> payloadPublisher.publishPayload(encodedPayload, recipientKey),
              EnhancedPrivacyNotSupportedException.class);
      assertThat(exception)
          .hasMessageContaining("Transactions with enhanced privacy is not currently supported");
      verify(discovery).getRemoteNodeInfo(eq(recipientKey));
    }

    payloadEncoderFactoryFunction.verify(
        times(2), () -> PayloadEncoder.create(any(EncodedPayloadCodec.class)));
  }

  @Test
  public void handleConnectionError() {

    final String targetUri = "http://jimmywhite.com";
    final PublicKey recipientKey = mock(PublicKey.class);

    Recipient recipient = mock(Recipient.class);
    when(recipient.getKey()).thenReturn(recipientKey);
    when(recipient.getUrl()).thenReturn(targetUri);

    NodeInfo nodeInfo = mock(NodeInfo.class);
    when(nodeInfo.getRecipients()).thenReturn(Set.of(recipient));
    when(nodeInfo.getUrl()).thenReturn(targetUri);
    when(discovery.getRemoteNodeInfo(recipientKey)).thenReturn(nodeInfo);

    Client client = mock(Client.class);
    when(client.target(targetUri)).thenThrow(ProcessingException.class);

    final EncodedPayload payload = mock(EncodedPayload.class);
    when(payload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
    when(payloadEncoder.encode(payload)).thenReturn("SomeData".getBytes());

    RestPayloadPublisher restPayloadPublisher = new RestPayloadPublisher(client, discovery);

    try {
      restPayloadPublisher.publishPayload(payload, recipientKey);
      failBecauseExceptionWasNotThrown(NodeOfflineException.class);
    } catch (NodeOfflineException ex) {
      assertThat(ex).hasMessageContaining(targetUri);
      verify(client).target(targetUri);
      verify(discovery).getRemoteNodeInfo(eq(recipientKey));
      verify(payloadEncoder).encode(payload);
      verify(discovery).getRemoteNodeInfo(eq(recipientKey));
      payloadEncoderFactoryFunction.verify(
          () -> PayloadEncoder.create(any(EncodedPayloadCodec.class)));
    }
  }

  @Test
  public void publishMandatoryRecipientsToNodesThatDoNotSupport() {

    String targetUrl = "http://someplace.com";

    EncodedPayload encodedPayload = mock(EncodedPayload.class);
    when(encodedPayload.getPrivacyMode()).thenReturn(PrivacyMode.MANDATORY_RECIPIENTS);
    byte[] payloadData = "Some Data".getBytes();
    when(payloadEncoder.encode(encodedPayload)).thenReturn(payloadData);

    PublicKey recipientKey = mock(PublicKey.class);
    NodeInfo nodeInfo = mock(NodeInfo.class);
    when(nodeInfo.supportedApiVersions()).thenReturn(Set.of("v2", "2.1", "3.0"));
    Recipient recipient = mock(Recipient.class);
    when(recipient.getKey()).thenReturn(recipientKey);
    when(recipient.getUrl()).thenReturn(targetUrl);

    when(nodeInfo.getRecipients()).thenReturn(Set.of(recipient));
    when(discovery.getRemoteNodeInfo(recipientKey)).thenReturn(nodeInfo);

    assertThatExceptionOfType(MandatoryRecipientsNotSupportedException.class)
        .isThrownBy(() -> payloadPublisher.publishPayload(encodedPayload, recipientKey))
        .withMessageContaining(
            "Transactions with mandatory recipients are not currently supported on recipient");

    verify(discovery).getRemoteNodeInfo(eq(recipientKey));

    payloadEncoderFactoryFunction.verify(
        () -> PayloadEncoder.create(any(EncodedPayloadCodec.class)));
  }
}
