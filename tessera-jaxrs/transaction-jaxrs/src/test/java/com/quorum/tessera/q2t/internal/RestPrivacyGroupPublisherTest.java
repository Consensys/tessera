package com.quorum.tessera.q2t.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.*;

import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.privacygroup.exception.PrivacyGroupNotSupportedException;
import com.quorum.tessera.privacygroup.exception.PrivacyGroupPublishException;
import com.quorum.tessera.privacygroup.publish.PrivacyGroupPublisher;
import com.quorum.tessera.transaction.publish.NodeOfflineException;
import com.quorum.tessera.version.MultiTenancyVersion;
import com.quorum.tessera.version.PrivacyGroupVersion;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class RestPrivacyGroupPublisherTest {

  private PrivacyGroupPublisher publisher;

  private Discovery discovery;

  private Client client;

  @Before
  public void setUp() {
    client = mock(Client.class);
    discovery = mock(Discovery.class);
    publisher = new RestPrivacyGroupPublisher(discovery, client);
  }

  @After
  public void afterTest() {
    verifyNoMoreInteractions(client, discovery);
  }

  @Test
  public void testPublishSingleSuccess() {

    String targetUrl = "https://sometargeturl.com";
    PublicKey recipient = PublicKey.from("PUBLIC_KEY".getBytes());
    NodeInfo nodeInfo = mock(NodeInfo.class);
    when(nodeInfo.supportedApiVersions()).thenReturn(Set.of(PrivacyGroupVersion.API_VERSION_3));
    when(nodeInfo.getUrl()).thenReturn(targetUrl);

    when(discovery.getRemoteNodeInfo(recipient)).thenReturn(nodeInfo);

    WebTarget webTarget = mock(WebTarget.class);
    when(client.target(targetUrl)).thenReturn(webTarget);
    when(webTarget.path(anyString())).thenReturn(webTarget);
    Invocation.Builder invocationBuilder = mock(Invocation.Builder.class);
    when(webTarget.request()).thenReturn(invocationBuilder);

    Response response = Response.ok().build();
    ArgumentCaptor<Entity> argumentCaptor = ArgumentCaptor.forClass(Entity.class);

    when(invocationBuilder.post(argumentCaptor.capture())).thenReturn(response);

    final byte[] data = new byte[] {15};
    publisher.publishPrivacyGroup(data, recipient);

    assertThat(argumentCaptor.getAllValues()).hasSize(1);
    Entity<byte[]> result = argumentCaptor.getValue();
    assertThat(result.getEntity()).isSameAs(data);
    assertThat(result.getMediaType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM_TYPE);

    verify(discovery).getRemoteNodeInfo(recipient);
    verify(client).target(targetUrl);
  }

  @Test
  public void testPublishSingleError() {

    String targetUrl = "https://sometargeturl.com";
    PublicKey recipient = PublicKey.from("PUBLIC_KEY".getBytes());
    NodeInfo nodeInfo = mock(NodeInfo.class);
    when(nodeInfo.supportedApiVersions()).thenReturn(Set.of(PrivacyGroupVersion.API_VERSION_3));
    when(nodeInfo.getUrl()).thenReturn(targetUrl);

    when(discovery.getRemoteNodeInfo(recipient)).thenReturn(nodeInfo);

    Invocation.Builder invocationBuilder = mock(Invocation.Builder.class);
    when(invocationBuilder.post(any(Entity.class))).thenReturn(Response.serverError().build());

    WebTarget webTarget = mock(WebTarget.class);
    when(client.target(targetUrl)).thenReturn(webTarget);
    when(webTarget.path(anyString())).thenReturn(webTarget);
    when(webTarget.request()).thenReturn(invocationBuilder);

    final byte[] data = new byte[5];
    try {
      publisher.publishPrivacyGroup(data, recipient);
      failBecauseExceptionWasNotThrown(PrivacyGroupPublishException.class);
    } catch (PrivacyGroupPublishException ex) {
      verify(discovery).getRemoteNodeInfo(recipient);
      verify(client).target(targetUrl);
    }
  }

  @Test
  public void testPublishSingleNodeOffline() {

    String targetUrl = "https://sometargeturl.com";
    PublicKey recipient = PublicKey.from("PUBLIC_KEY".getBytes());
    NodeInfo nodeInfo = mock(NodeInfo.class);
    when(nodeInfo.supportedApiVersions()).thenReturn(Set.of(PrivacyGroupVersion.API_VERSION_3));
    when(nodeInfo.getUrl()).thenReturn(targetUrl);

    when(discovery.getRemoteNodeInfo(recipient)).thenReturn(nodeInfo);

    Invocation.Builder invocationBuilder = mock(Invocation.Builder.class);
    when(invocationBuilder.post(any(Entity.class))).thenThrow(ProcessingException.class);

    WebTarget webTarget = mock(WebTarget.class);
    when(client.target(targetUrl)).thenReturn(webTarget);
    when(webTarget.path(anyString())).thenReturn(webTarget);
    when(webTarget.request()).thenReturn(invocationBuilder);

    final byte[] data = new byte[5];
    try {
      publisher.publishPrivacyGroup(data, PublicKey.from("PUBLIC_KEY".getBytes()));
      failBecauseExceptionWasNotThrown(NodeOfflineException.class);
    } catch (NodeOfflineException ex) {
      verify(discovery).getRemoteNodeInfo(recipient);
      verify(client).target(targetUrl);
    }
  }

  @Test
  public void remoteNodeDoesNotSupportPrivacyGroup() {

    String targetUrl = "url2.com";
    final List<String> versions = List.of(MultiTenancyVersion.API_VERSION_2_1);

    final NodeInfo oldNode =
        NodeInfo.Builder.create()
            .withUrl(targetUrl)
            .withRecipients(Collections.emptyList())
            .withSupportedApiVersions(versions)
            .build();
    PublicKey recipient = PublicKey.from("OLD_KEY".getBytes());

    when(discovery.getRemoteNodeInfo(recipient)).thenReturn(oldNode);

    final byte[] data = new byte[] {15};

    try {
      publisher.publishPrivacyGroup(data, recipient);
    } catch (PrivacyGroupNotSupportedException privacyGroupNotSupportedException) {
      assertThat(privacyGroupNotSupportedException)
          .isNotNull()
          .hasMessageContaining(recipient.encodeToBase64());
      verify(discovery).getRemoteNodeInfo(recipient);
    }
  }
}
