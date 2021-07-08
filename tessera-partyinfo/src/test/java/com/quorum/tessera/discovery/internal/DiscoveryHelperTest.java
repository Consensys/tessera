package com.quorum.tessera.discovery.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.discovery.ActiveNode;
import com.quorum.tessera.discovery.DiscoveryHelper;
import com.quorum.tessera.discovery.NetworkStore;
import com.quorum.tessera.discovery.NodeUri;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.encryption.KeyNotFoundException;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.partyinfo.node.Recipient;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

public class DiscoveryHelperTest {

  private Enclave enclave;

  private NetworkStore networkStore;

  private RuntimeContext runtimeContext;

  private DiscoveryHelper discoveryHelper;

  private MockedStatic<RuntimeContext> mockedRuntimeContext;

  @Before
  public void beforeTest() {
    this.runtimeContext = mock(RuntimeContext.class);
    mockedRuntimeContext = mockStatic(RuntimeContext.class);
    mockedRuntimeContext.when(RuntimeContext::getInstance).thenReturn(runtimeContext);

    this.enclave = mock(Enclave.class);
    this.networkStore = mock(NetworkStore.class);
    this.discoveryHelper = new DiscoveryHelperImpl(networkStore, enclave);
  }

  @After
  public void afterTest() {
    verifyNoMoreInteractions(enclave, networkStore, runtimeContext);
    mockedRuntimeContext.verifyNoMoreInteractions();
    mockedRuntimeContext.close();
  }

  @Test
  public void onCreate() {
    URI uri = URI.create("http://somedomain.com/");
    when(runtimeContext.getPeers()).thenReturn(List.of(uri));

    when(runtimeContext.getP2pServerUri()).thenReturn(uri);

    PublicKey publicKey = mock(PublicKey.class);
    when(enclave.getPublicKeys()).thenReturn(Set.of(publicKey));

    discoveryHelper.onCreate();

    verify(networkStore).store(any(ActiveNode.class));
    verify(runtimeContext).getP2pServerUri();
    verify(enclave).getPublicKeys();

    mockedRuntimeContext.verify(RuntimeContext::getInstance);
  }

  @Test
  public void buildCurrent() {

    final URI uri = URI.create("http://somedomain.com");
    when(runtimeContext.getP2pServerUri()).thenReturn(uri);

    final List<PublicKey> keys =
        IntStream.range(0, 5).mapToObj(i -> mock(PublicKey.class)).collect(Collectors.toList());

    final ActiveNode activeNode =
        ActiveNode.Builder.create().withUri(NodeUri.create(uri)).withKeys(keys).build();

    when(networkStore.getActiveNodes()).thenReturn(Stream.of(activeNode));

    NodeInfo result = discoveryHelper.buildCurrent();
    assertThat(result).isNotNull();

    assertThat(result.getUrl()).isEqualTo("http://somedomain.com/");
    assertThat(result.getRecipients()).hasSize(5);

    List<Recipient> recipients = List.copyOf(result.getRecipients());
    assertThat(recipients.stream().map(Recipient::getKey).collect(Collectors.toList()))
        .containsExactlyInAnyOrderElementsOf(keys);

    verify(networkStore).getActiveNodes();
    verify(runtimeContext).getP2pServerUri();
    mockedRuntimeContext.verify(RuntimeContext::getInstance);
  }

  @Test
  public void getCurrentWithNoKeys() {

    final URI uri = URI.create("http://somedomain.com");
    when(runtimeContext.getP2pServerUri()).thenReturn(uri);

    final List<PublicKey> keys = List.of();

    final ActiveNode activeNode =
        ActiveNode.Builder.create().withUri(NodeUri.create(uri)).withKeys(keys).build();

    when(networkStore.getActiveNodes()).thenReturn(Stream.of(activeNode));

    NodeInfo result = discoveryHelper.buildCurrent();
    assertThat(result).isNotNull();
    verify(runtimeContext).getP2pServerUri();

    assertThat(result.getUrl()).isEqualTo("http://somedomain.com/");
    verify(networkStore).getActiveNodes();
    assertThat(result.getRecipients()).isEmpty();
    mockedRuntimeContext.verify(RuntimeContext::getInstance);
  }

  @Test
  public void getCurrentWithUriOnly() {

    final URI uri = URI.create("http://somedomain.com");
    when(runtimeContext.getP2pServerUri()).thenReturn(uri);

    NodeInfo result = discoveryHelper.buildCurrent();
    assertThat(result).isNotNull();
    verify(runtimeContext).getP2pServerUri();

    assertThat(result.getUrl()).isEqualTo("http://somedomain.com/");
    assertThat(result.getRecipients()).isEmpty();
    verify(networkStore).getActiveNodes();
    mockedRuntimeContext.verify(RuntimeContext::getInstance);
  }

  @Test
  public void buildRemoteNodeInfo() {

    String url = "http://nodeurl.com/";

    final PublicKey key = PublicKey.from("key".getBytes());
    final PublicKey anotherKey = PublicKey.from("anotherKey".getBytes());

    final Recipient recipient = Recipient.of(key, url);
    final Recipient sameNodeDifferentKey = Recipient.of(anotherKey, url);

    ActiveNode activeNode = mock(ActiveNode.class);
    when(activeNode.getUri()).thenReturn(NodeUri.create(url));
    when(activeNode.getKeys()).thenReturn(Set.of(key, anotherKey));
    when(activeNode.getSupportedVersions()).thenReturn(Set.of("v1", "v2"));

    when(networkStore.getActiveNodes()).thenReturn(Stream.of(activeNode));

    final NodeInfo result = discoveryHelper.buildRemoteNodeInfo(key);

    assertThat(result).isNotNull();
    assertThat(result.getUrl()).isEqualTo(url);
    assertThat(result.getRecipients()).containsExactlyInAnyOrder(recipient, sameNodeDifferentKey);
    assertThat(result.supportedApiVersions()).containsExactlyInAnyOrder("v1", "v2");
    verify(networkStore).getActiveNodes();
  }

  @Test
  public void recipientKeyNotFound() {
    String url = "http://nodeurl.com/";

    final PublicKey key = PublicKey.from("key".getBytes());
    final PublicKey anotherKey = PublicKey.from("anotherKey".getBytes());

    ActiveNode activeNode = mock(ActiveNode.class);
    when(activeNode.getUri()).thenReturn(NodeUri.create(url));
    when(activeNode.getKeys()).thenReturn(Set.of(key));
    when(activeNode.getSupportedVersions()).thenReturn(Set.of("v1", "v2"));

    when(networkStore.getActiveNodes()).thenReturn(Stream.of(activeNode));

    assertThatExceptionOfType(KeyNotFoundException.class)
        .isThrownBy(() -> discoveryHelper.buildRemoteNodeInfo(anotherKey));

    verify(networkStore).getActiveNodes();
  }

  @Test
  public void buildAllNodeInfos() {

    when(runtimeContext.getP2pServerUri()).thenReturn(URI.create("http://own.com"));

    final ActiveNode node1 =
        ActiveNode.Builder.create()
            .withUri(NodeUri.create("http://node1.com"))
            .withKeys(List.of(PublicKey.from("key1".getBytes())))
            .withSupportedVersions(List.of("v1"))
            .build();

    final ActiveNode node2 =
        ActiveNode.Builder.create()
            .withUri(NodeUri.create("http://node2.com"))
            .withKeys(List.of(PublicKey.from("key2".getBytes())))
            .withSupportedVersions(List.of("v2"))
            .build();

    when(networkStore.getActiveNodes()).thenReturn(Stream.of(node1, node2));

    final Set<NodeInfo> nodeInfos = discoveryHelper.buildRemoteNodeInfos();

    assertThat(nodeInfos).hasSize(2);

    Set<ActiveNode> activeNodes =
        nodeInfos.stream()
            .map(
                nodeInfo ->
                    ActiveNode.Builder.create()
                        .withUri(NodeUri.create(nodeInfo.getUrl()))
                        .withKeys(
                            nodeInfo.getRecipients().stream()
                                .map(Recipient::getKey)
                                .collect(Collectors.toSet()))
                        .withSupportedVersions(nodeInfo.supportedApiVersions())
                        .build())
            .collect(Collectors.toSet());

    assertThat(activeNodes).containsExactlyInAnyOrder(node1, node2);

    verify(networkStore).getActiveNodes();
    verify(runtimeContext).getP2pServerUri();
    mockedRuntimeContext.verify(RuntimeContext::getInstance);
  }

  @Test
  public void buildAllNodeInfosFilteredOutOwn() {

    when(runtimeContext.getP2pServerUri()).thenReturn(URI.create("http://node1.com"));

    final ActiveNode node1 =
        ActiveNode.Builder.create()
            .withUri(NodeUri.create("http://node1.com"))
            .withKeys(List.of(PublicKey.from("key1".getBytes())))
            .withSupportedVersions(List.of("v1"))
            .build();

    final ActiveNode node2 =
        ActiveNode.Builder.create()
            .withUri(NodeUri.create("http://node2.com"))
            .withKeys(List.of(PublicKey.from("key2".getBytes())))
            .withSupportedVersions(List.of("v2"))
            .build();

    when(networkStore.getActiveNodes()).thenReturn(Stream.of(node1, node2));

    final Set<NodeInfo> nodeInfos = discoveryHelper.buildRemoteNodeInfos();

    assertThat(nodeInfos).hasSize(1);

    Set<ActiveNode> activeNodes =
        nodeInfos.stream()
            .map(
                nodeInfo ->
                    ActiveNode.Builder.create()
                        .withUri(NodeUri.create(nodeInfo.getUrl()))
                        .withKeys(
                            nodeInfo.getRecipients().stream()
                                .map(Recipient::getKey)
                                .collect(Collectors.toSet()))
                        .withSupportedVersions(nodeInfo.supportedApiVersions())
                        .build())
            .collect(Collectors.toSet());

    assertThat(activeNodes).containsExactlyInAnyOrder(node2);

    verify(networkStore).getActiveNodes();
    verify(runtimeContext).getP2pServerUri();
    mockedRuntimeContext.verify(RuntimeContext::getInstance);
  }

  @Test
  public void create() {
    try (var staticEnclave = mockStatic(Enclave.class)) {
      Enclave enclave = mock(Enclave.class);
      staticEnclave.when(Enclave::create).thenReturn(enclave);
      DiscoveryHelper.create();

      staticEnclave.verify(Enclave::create);
      verifyNoInteractions(enclave);
      staticEnclave.verifyNoMoreInteractions();
    }
  }
}
