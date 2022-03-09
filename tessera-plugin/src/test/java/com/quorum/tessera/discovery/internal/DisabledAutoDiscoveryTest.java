package com.quorum.tessera.discovery.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.discovery.ActiveNode;
import com.quorum.tessera.discovery.NetworkStore;
import com.quorum.tessera.discovery.NodeUri;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.AutoDiscoveryDisabledException;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.partyinfo.node.Recipient;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DisabledAutoDiscoveryTest {

  private DisabledAutoDiscovery discovery;

  private NetworkStore networkStore;

  private RuntimeContext runtimeContext;

  private Set<NodeUri> knownPeers;

  @Before
  public void onSetUp() {
    networkStore = mock(NetworkStore.class);
    knownPeers =
        Stream.of("http://bobbysixkiller.com", "http://renoraynes.com")
            .map(NodeUri::create)
            .collect(Collectors.toSet());

    discovery = new DisabledAutoDiscovery(networkStore, knownPeers);

    runtimeContext = mock(RuntimeContext.class);
  }

  @After
  public void onTearDown() {
    verifyNoMoreInteractions(networkStore, runtimeContext);
  }

  @Test
  public void onUpdateFromUnknownPeer() {
    NodeInfo nodeInfo = NodeInfo.Builder.create().withUrl("http://donalddutchdixon.com").build();

    try {
      discovery.onUpdate(nodeInfo);
      failBecauseExceptionWasNotThrown(AutoDiscoveryDisabledException.class);
    } catch (AutoDiscoveryDisabledException exception) {
      assertThat(exception).isNotNull();
    }
  }

  @Test
  public void onUpdateFromKnownPeer() {
    NodeInfo nodeInfo =
        NodeInfo.Builder.create().withUrl(knownPeers.iterator().next().asString()).build();

    discovery.onUpdate(nodeInfo);

    verify(networkStore).store(any(ActiveNode.class));
  }

  @Test
  public void onUpdateNodeSendsNewKey() {

    String url = knownPeers.iterator().next().asString();

    PublicKey key = mock(PublicKey.class);

    List<Recipient> recipients = List.of(Recipient.of(key, url));

    List<ActiveNode> storedNodes = new ArrayList<>();
    doAnswer(
            invocation -> {
              storedNodes.add(invocation.getArgument(0));
              return null;
            })
        .when(networkStore)
        .store(any(ActiveNode.class));

    NodeInfo nodeInfo = NodeInfo.Builder.create().withUrl(url).withRecipients(recipients).build();

    discovery.onUpdate(nodeInfo);

    assertThat(storedNodes).hasSize(1);

    ActiveNode savedNode = storedNodes.get(0);

    assertThat(savedNode.getKeys()).containsExactly(key);
    assertThat(savedNode.getUri()).isEqualTo(NodeUri.create(url));

    verify(networkStore).store(any(ActiveNode.class));
  }

  @Test
  public void onUpdateIgnoreKeysNotOwnedBySender() {
    String uri = "http://renoraynes.com";

    PublicKey key = mock(PublicKey.class);
    PublicKey key2 = mock(PublicKey.class);
    Recipient recipient = Recipient.of(key, uri);
    Recipient other = Recipient.of(key2, "http://othernode.com");
    List<Recipient> recipients = List.of(recipient, other);

    NodeInfo nodeInfo =
        NodeInfo.Builder.create()
            .withUrl(uri)
            .withRecipients(recipients)
            .withSupportedApiVersions(List.of("Two", "Fifty"))
            .build();

    List<ActiveNode> storedNodes = new ArrayList<>();
    doAnswer(
            invocation -> {
              storedNodes.add(invocation.getArgument(0));
              return null;
            })
        .when(networkStore)
        .store(any(ActiveNode.class));

    discovery.onUpdate(nodeInfo);

    assertThat(storedNodes).hasSize(1);

    ActiveNode result = storedNodes.iterator().next();

    assertThat(result.getUri()).isEqualTo(NodeUri.create(uri));
    assertThat(result.getKeys()).containsExactly(key);
    assertThat(result.getSupportedVersions()).containsExactlyInAnyOrder("Two", "Fifty");
    verify(networkStore).store(any(ActiveNode.class));
  }

  @Test
  public void onDisconnect() {
    URI uri = URI.create("http://onDisconnect.com");
    List<NodeUri> results = new ArrayList<>();
    doAnswer(
            invocation -> {
              results.add(invocation.getArgument(0));
              return null;
            })
        .when(networkStore)
        .remove(any(NodeUri.class));

    discovery.onDisconnect(uri);

    assertThat(results).containsExactly(NodeUri.create(uri));
    verify(networkStore).remove(any(NodeUri.class));
  }
}
