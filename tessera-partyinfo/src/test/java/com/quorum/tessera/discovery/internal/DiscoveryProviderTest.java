package com.quorum.tessera.discovery.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.discovery.Discovery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

public class DiscoveryProviderTest {

  private MockedStatic<RuntimeContext> mockedRuntimeContext;

  private RuntimeContext runtimeContext;

  @Before
  public void beforeTest() {
    runtimeContext = mock(RuntimeContext.class);
    mockedRuntimeContext = mockStatic(RuntimeContext.class);
    mockedRuntimeContext.when(RuntimeContext::getInstance).thenReturn(runtimeContext);
  }

  @After
  public void afterTest() {
    verifyNoMoreInteractions(runtimeContext);
    mockedRuntimeContext.verifyNoMoreInteractions();
    mockedRuntimeContext.close();
  }

  @Test
  public void provideAutoDiscovery() {

    when(runtimeContext.isDisablePeerDiscovery()).thenReturn(false);

    Discovery discovery = DiscoveryProvider.provider();
    assertThat(discovery).isNotNull().isExactlyInstanceOf(AutoDiscovery.class);

    verify(runtimeContext).isDisablePeerDiscovery();
    mockedRuntimeContext.verify(RuntimeContext::getInstance);
  }

  @Test
  public void provideDisabledAutoDiscovery() {

    when(runtimeContext.isDisablePeerDiscovery()).thenReturn(true);

    Discovery discovery = DiscoveryProvider.provider();

    assertThat(discovery).isNotNull().isExactlyInstanceOf(DisabledAutoDiscovery.class);

    verify(runtimeContext).isDisablePeerDiscovery();
    verify(runtimeContext).getPeers();
    mockedRuntimeContext.verify(RuntimeContext::getInstance);
  }

  @Test
  public void defaultConstructor() {
    DiscoveryProvider discoveryFactory = new DiscoveryProvider();
    assertThat(discoveryFactory).isNotNull();
  }
}
