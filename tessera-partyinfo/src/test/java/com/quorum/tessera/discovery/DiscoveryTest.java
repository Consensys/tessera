package com.quorum.tessera.discovery;

import static org.mockito.Mockito.*;

import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.serviceloader.ServiceLoaderUtil;
import java.net.URI;
import java.util.ServiceLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

public class DiscoveryTest {

  private Discovery discovery;

  private MockedStatic<DiscoveryHelper> mockedStaticDiscoveryHelper;

  private DiscoveryHelper discoveryHelper;

  @Before
  public void beforeTest() {
    discovery =
        new Discovery() {
          @Override
          public void onUpdate(NodeInfo nodeInfo) {}

          @Override
          public void onDisconnect(URI nodeUri) {}
        };

    discoveryHelper = mock(DiscoveryHelper.class);
    mockedStaticDiscoveryHelper = mockStatic(DiscoveryHelper.class);
    mockedStaticDiscoveryHelper.when(DiscoveryHelper::create).thenReturn(discoveryHelper);
  }

  @After
  public void afterTest() {
    verifyNoMoreInteractions(discoveryHelper);
    mockedStaticDiscoveryHelper.verifyNoMoreInteractions();
    mockedStaticDiscoveryHelper.close();
  }

  @Test
  public void onCreate() {
    discovery.onCreate();
    verify(discoveryHelper).onCreate();
    mockedStaticDiscoveryHelper.verify(DiscoveryHelper::create);
  }

  @Test
  public void getCurrent() {
    discovery.getCurrent();
    verify(discoveryHelper).buildCurrent();
    mockedStaticDiscoveryHelper.verify(DiscoveryHelper::create);
  }

  @Test
  public void getRemoteNodeInfo() {
    PublicKey publicKey = mock(PublicKey.class);
    discovery.getRemoteNodeInfo(publicKey);
    verify(discoveryHelper).buildRemoteNodeInfo(publicKey);
    mockedStaticDiscoveryHelper.verify(DiscoveryHelper::create);
  }

  @Test
  public void getRemoteNodeInfos() {
    discovery.getRemoteNodeInfos();
    verify(discoveryHelper).buildRemoteNodeInfos();
    mockedStaticDiscoveryHelper.verify(DiscoveryHelper::create);
  }

  @Test
  public void create() {
    try (var serviceLoaderUtilMockedStatic = mockStatic(ServiceLoaderUtil.class);
        var serviceLoaderMockedStatic = mockStatic(ServiceLoader.class)) {

      ServiceLoader<Discovery> serviceLoader = mock(ServiceLoader.class);
      serviceLoaderMockedStatic
          .when(() -> ServiceLoader.load(Discovery.class))
          .thenReturn(serviceLoader);

      Discovery.create();

      serviceLoaderUtilMockedStatic.verify(() -> ServiceLoaderUtil.loadSingle(serviceLoader));
      serviceLoaderUtilMockedStatic.verifyNoMoreInteractions();

      serviceLoaderMockedStatic.verify(() -> ServiceLoader.load(Discovery.class));
      serviceLoaderMockedStatic.verifyNoMoreInteractions();
      verifyNoInteractions(serviceLoader);
    }
  }
}
