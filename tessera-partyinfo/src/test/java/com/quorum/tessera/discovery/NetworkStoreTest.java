package com.quorum.tessera.discovery;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.discovery.internal.DefaultNetworkStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NetworkStoreTest {

  private NetworkStore networkStore;

  @Before
  public void setUp() {
    networkStore = NetworkStore.getInstance();
    assertThat(networkStore).isExactlyInstanceOf(DefaultNetworkStore.class);
    networkStore.getActiveNodes().map(ActiveNode::getUri).forEach(networkStore::remove);
  }

  @After
  public void tearDown() {
    networkStore.getActiveNodes().map(ActiveNode::getUri).forEach(networkStore::remove);
  }

  @Test
  public void storeAndRemoveNode() {

    NodeUri nodeUri = NodeUri.create("http://someaddress.com");
    ActiveNode activeNode = ActiveNode.Builder.create().withUri(nodeUri).build();

    networkStore.store(activeNode);

    assertThat(networkStore.getActiveNodes()).containsExactly(activeNode);

    networkStore.remove(nodeUri);

    assertThat(networkStore.getActiveNodes()).isEmpty();
  }

  @Test
  public void storeTwoNodes() {
    NodeUri nodeUri = NodeUri.create("http://someaddress.com");
    NodeUri someOtherNodeUri = NodeUri.create("http://someotheraddress.com");

    networkStore.store(ActiveNode.Builder.create().withUri(nodeUri).build());

    networkStore.store(ActiveNode.Builder.create().withUri(someOtherNodeUri).build());

    assertThat(networkStore.getActiveNodes().count()).isEqualTo(2L);
  }
}
