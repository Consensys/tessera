package com.quorum.tessera.discovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import java.net.URI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DiscoveryTest {

  private RuntimeContext runtimeContext;

  @Before
  public void onSetUp() {
    runtimeContext = RuntimeContext.getInstance();
    MockDiscoveryHelper.reset();
  }

  @After
  public void onTearDown() {
    verifyNoMoreInteractions(runtimeContext);
    MockDiscoveryHelper.reset();
  }

  @Test
  public void getInstance() {
    Discovery instance = Discovery.getInstance();
    assertThat(instance).isExactlyInstanceOf(DiscoveryFactory.class);
    verify(runtimeContext).isDisablePeerDiscovery();
  }

  @Test
  public void onCreate() {

    Discovery discovery =
        new Discovery() {
          @Override
          public void onUpdate(NodeInfo nodeInfo) {
            throw new UnsupportedOperationException();
          }

          @Override
          public void onDisconnect(URI nodeUri) {
            throw new UnsupportedOperationException();
          }
        };

    MockDiscoveryHelper discoveryHelper =
        MockDiscoveryHelper.class.cast(DiscoveryHelper.getInstance());
    discovery.onCreate();
    assertThat(discoveryHelper.getOnCreateInvocationCount()).isEqualTo(1);
    discovery.onCreate();
    assertThat(discoveryHelper.getOnCreateInvocationCount()).isEqualTo(2);
  }

  @Test
  public void getCurrent() {

    Discovery discovery =
        new Discovery() {
          @Override
          public void onUpdate(NodeInfo nodeInfo) {
            throw new UnsupportedOperationException();
          }

          @Override
          public void onDisconnect(URI nodeUri) {
            throw new UnsupportedOperationException();
          }
        };

    MockDiscoveryHelper discoveryHelper =
        MockDiscoveryHelper.class.cast(DiscoveryHelper.getInstance());
    discovery.getCurrent();
    assertThat(discoveryHelper.getBuildCurrentInvocationCounter()).isEqualTo(1);
    discovery.getCurrent();
    assertThat(discoveryHelper.getBuildCurrentInvocationCounter()).isEqualTo(2);
  }

  @Test
  public void getRemoteNodeInfo() {

    Discovery discovery =
        new Discovery() {
          @Override
          public void onUpdate(NodeInfo nodeInfo) {
            throw new UnsupportedOperationException();
          }

          @Override
          public void onDisconnect(URI nodeUri) {
            throw new UnsupportedOperationException();
          }
        };

    MockDiscoveryHelper discoveryHelper =
        MockDiscoveryHelper.class.cast(DiscoveryHelper.getInstance());
    discovery.getRemoteNodeInfo(mock(PublicKey.class));
    assertThat(discoveryHelper.getBuildRemoteInvocationCounter()).isEqualTo(1);
    discovery.getRemoteNodeInfo(mock(PublicKey.class));
    assertThat(discoveryHelper.getBuildRemoteInvocationCounter()).isEqualTo(2);
  }

  @Test
  public void getAllNodeInfos() {

    Discovery discovery =
        new Discovery() {
          @Override
          public void onUpdate(NodeInfo nodeInfo) {
            throw new UnsupportedOperationException();
          }

          @Override
          public void onDisconnect(URI nodeUri) {
            throw new UnsupportedOperationException();
          }
        };

    MockDiscoveryHelper discoveryHelper =
        MockDiscoveryHelper.class.cast(DiscoveryHelper.getInstance());
    discovery.getRemoteNodeInfos();
    assertThat(discoveryHelper.getBuildAllInvocationCounter()).isEqualTo(1);
    discovery.getRemoteNodeInfos();
    assertThat(discoveryHelper.getBuildAllInvocationCounter()).isEqualTo(2);
  }
}
