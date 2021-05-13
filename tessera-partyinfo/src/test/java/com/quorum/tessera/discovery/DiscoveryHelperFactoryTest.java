package com.quorum.tessera.discovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.encryption.PublicKey;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DiscoveryHelperFactoryTest {

  private DiscoveryHelperFactory discoveryHelperFactory;

  private DiscoveryHelper discoveryHelper;

  @Before
  public void beforeTest() {
    discoveryHelper = mock(DiscoveryHelper.class);
    discoveryHelperFactory = new DiscoveryHelperFactory(discoveryHelper);
  }

  @After
  public void afterTest() {
    verifyNoMoreInteractions(discoveryHelper);
  }

  @Test
  public void onCreate() {
    discoveryHelperFactory.onCreate();
    verify(discoveryHelper).onCreate();
  }

  @Test
  public void buildCurrent() {
    discoveryHelperFactory.buildCurrent();
    verify(discoveryHelper).buildCurrent();
  }

  @Test
  public void buildRemoteNodeInfo() {
    PublicKey key = mock(PublicKey.class);
    discoveryHelperFactory.buildRemoteNodeInfo(key);
    verify(discoveryHelper).buildRemoteNodeInfo(key);
  }

  @Test
  public void buildAllNodeInfos() {
    discoveryHelperFactory.buildRemoteNodeInfos();
    verify(discoveryHelper).buildRemoteNodeInfos();
  }

  @Test
  public void provider() {
    DiscoveryHelper helper = DiscoveryHelperFactory.provider();
    assertThat(helper).isNotNull().isExactlyInstanceOf(DiscoveryHelperImpl.class);
  }

  @Test
  public void defaultConstructor() {
    DiscoveryHelper helper = new DiscoveryHelperFactory();
    assertThat(helper).isNotNull().isExactlyInstanceOf(DiscoveryHelperFactory.class);
  }
}
