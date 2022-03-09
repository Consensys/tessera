package com.quorum.tessera.discovery.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.discovery.Discovery;
import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

public class DiscoveryProviderTest {

  private MockedStatic<DiscoveryHolder> discoveryHolderMockedStatic;

  private DiscoveryHolder discoveryHolder;

  private MockedStatic<RuntimeContext> mockedRuntimeContext;

  private RuntimeContext runtimeContext;

  @Before
  public void beforeTest() {
    discoveryHolder = mock(DiscoveryHolder.class);
    when(discoveryHolder.get()).thenReturn(Optional.empty());
    discoveryHolderMockedStatic = mockStatic(DiscoveryHolder.class);
    discoveryHolderMockedStatic.when(DiscoveryHolder::create).thenReturn(discoveryHolder);

    runtimeContext = mock(RuntimeContext.class);
    mockedRuntimeContext = mockStatic(RuntimeContext.class);
    mockedRuntimeContext.when(RuntimeContext::getInstance).thenReturn(runtimeContext);
  }

  @After
  public void afterTest() {

    verifyNoMoreInteractions(discoveryHolder);
    discoveryHolderMockedStatic.verifyNoMoreInteractions();
    discoveryHolderMockedStatic.close();

    verifyNoMoreInteractions(runtimeContext);
    mockedRuntimeContext.verifyNoMoreInteractions();
    mockedRuntimeContext.close();
  }

  @Test
  public void provideAutoDiscovery() {

    when(runtimeContext.isDisablePeerDiscovery()).thenReturn(false);

    Discovery discovery = DiscoveryProvider.provider();
    assertThat(discovery).isNotNull().isExactlyInstanceOf(AutoDiscovery.class);

    verify(discoveryHolder).get();
    verify(discoveryHolder).set(discovery);
    discoveryHolderMockedStatic.verify(DiscoveryHolder::create);

    verify(runtimeContext).isDisablePeerDiscovery();
    mockedRuntimeContext.verify(RuntimeContext::getInstance);
  }

  @Test
  public void provideStoredDiscovery() {

    Discovery discovery = mock(Discovery.class);
    when(discoveryHolder.get()).thenReturn(Optional.of(discovery));
    Discovery result = DiscoveryProvider.provider();
    assertThat(result).isSameAs(discovery);
    verify(discoveryHolder, times(2)).get();
    discoveryHolderMockedStatic.verify(DiscoveryHolder::create);
  }

  @Test
  public void provideDisabledAutoDiscovery() {

    when(runtimeContext.isDisablePeerDiscovery()).thenReturn(true);

    Discovery discovery = DiscoveryProvider.provider();

    assertThat(discovery).isNotNull().isExactlyInstanceOf(DisabledAutoDiscovery.class);

    verify(discoveryHolder).get();
    verify(discoveryHolder).set(discovery);
    discoveryHolderMockedStatic.verify(DiscoveryHolder::create);

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
