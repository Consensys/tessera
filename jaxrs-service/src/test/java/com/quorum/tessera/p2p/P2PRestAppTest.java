package com.quorum.tessera.p2p;

import com.quorum.tessera.api.filter.IPWhitelistFilter;
import com.quorum.tessera.service.locator.ServiceLocator;
import java.util.HashSet;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class P2PRestAppTest {

  private static final String CONTEXT_NAME = "context";

  private ServiceLocator serviceLocator;

  private P2PRestApp p2PRestApp;

  @Before
  public void setUp() {
    serviceLocator = mock(ServiceLocator.class);
    p2PRestApp = new P2PRestApp(serviceLocator, CONTEXT_NAME);
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(serviceLocator);
  }

  @Test
  public void getSingletons() {

    Set services = new HashSet<>();
    services.add(mock(PartyInfoResource.class));
    services.add(mock(IPWhitelistFilter.class));
    services.add(mock(TransactionResource.class));

    when(serviceLocator.getServices(CONTEXT_NAME)).thenReturn(services);

    Set<Object> results = p2PRestApp.getSingletons();

    assertThat(results).containsAll(services).hasSize(services.size() + 1);

    assertThat(results).filteredOn(o -> ApiResource.class.isInstance(o)).hasSize(1);

    verify(serviceLocator).getServices(CONTEXT_NAME);
  }
}
