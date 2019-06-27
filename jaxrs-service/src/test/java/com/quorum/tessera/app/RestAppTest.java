package com.quorum.tessera.app;

import com.quorum.tessera.p2p.P2PRestApp;
import com.quorum.tessera.service.locator.ServiceLocator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import static org.mockito.Mockito.*;

public class RestAppTest {

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
    p2PRestApp.getSingletons();
    verify(serviceLocator).getServices(CONTEXT_NAME);
  }

  @Test
  public void createWithNoServiceLocator() {

    final Throwable throwable = catchThrowable(() -> new P2PRestApp(null, CONTEXT_NAME));
    assertThat(throwable).isInstanceOf(NullPointerException.class);

    final Throwable throwableName = catchThrowable(() -> new P2PRestApp(serviceLocator, null));
    assertThat(throwableName).isInstanceOf(NullPointerException.class);
  }
}
