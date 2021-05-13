package com.quorum.tessera.enclave;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import org.junit.Test;

public class EnclaveServerProviderTest {

  @Test
  public void provider() {

    try (var configFactoryMockedStatic = mockStatic(ConfigFactory.class);
        var enclaveFactoryMockedStatic = mockStatic(EnclaveFactoryImpl.class)) {
      ConfigFactory configFactory = mock(ConfigFactory.class);
      Config config = mock(Config.class);
      when(configFactory.getConfig()).thenReturn(config);

      Enclave enclave = mock(Enclave.class);
      enclaveFactoryMockedStatic
          .when(() -> EnclaveFactoryImpl.createServer(config))
          .thenReturn(enclave);
      configFactoryMockedStatic.when(ConfigFactory::create).thenReturn(configFactory);

      EnclaveServer result = EnclaveServerProvider.provider();

      assertThat(result).isExactlyInstanceOf(EnclaveServerImpl.class);

      enclaveFactoryMockedStatic.verify(() -> EnclaveFactoryImpl.createServer(config));
      enclaveFactoryMockedStatic.verifyNoMoreInteractions();

      verify(configFactory).getConfig();
      verifyNoMoreInteractions(configFactory);

      configFactoryMockedStatic.verify(ConfigFactory::create);

      configFactoryMockedStatic.verifyNoMoreInteractions();
    }
  }

  @Test
  public void defaultConstrutorForCOverage() {
    assertThat(new EnclaveServerProvider()).isNotNull();
  }
}
