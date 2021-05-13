package com.quorum.tessera.enclave.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.enclave.EnclaveClient;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class EnclaveClientProviderTest {

  private ConfigFactory configFactory;

  private AppType appType;

  public EnclaveClientProviderTest(AppType appType) {
    this.appType = appType;
  }

  @Before
  public void beforeTest() {
    configFactory = mock(ConfigFactory.class);
    Config config = mock(Config.class);
    ServerConfig serverConfig = mock(ServerConfig.class);
    when(serverConfig.getApp()).thenReturn(appType);
    when(serverConfig.getServerUri()).thenReturn(URI.create("someEnclaveServerUri"));
    when(config.getServerConfigs()).thenReturn(List.of(serverConfig));
    when(configFactory.getConfig()).thenReturn(config);
  }

  @After
  public void afterTest() {
    verifyNoMoreInteractions(configFactory);
  }

  @Test
  public void provider() {

    try (var configFactoryMockedStatic = mockStatic(ConfigFactory.class)) {
      configFactoryMockedStatic.when(ConfigFactory::create).thenReturn(configFactory);

      if (appType == AppType.ENCLAVE) {
        EnclaveClient enclaveClient = EnclaveClientProvider.provider();
        assertThat(enclaveClient).isNotNull();
      } else {
        Throwable ex = catchThrowable(() -> EnclaveClientProvider.provider());
        assertThat(ex).isExactlyInstanceOf(NoSuchElementException.class);
      }
      configFactoryMockedStatic.verify(ConfigFactory::create);
      configFactoryMockedStatic.verifyNoMoreInteractions();
    }
    verify(configFactory).getConfig();
  }

  @Test
  public void defaultConstructor() {
    assertThat(new EnclaveClientProvider()).isNotNull();
  }

  @Parameterized.Parameters
  public static Collection<AppType> appTypes() {
    return List.of(AppType.ENCLAVE, AppType.P2P);
  }
}
