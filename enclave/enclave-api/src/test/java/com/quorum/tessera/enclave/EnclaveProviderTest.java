package com.quorum.tessera.enclave;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.config.util.JaxbUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EnclaveProviderTest {

  @Before
  @After
  public void clearHolder() {
    DefaultEnclaveHolder.INSTANCE.reset();
  }

  @Test
  public void defaultConstructorForCoverage() {
    assertThat(new EnclaveProvider()).isNotNull();
  }

  @Test
  public void provider() {

    try (var staticConfigFactory = mockStatic(ConfigFactory.class)) {

      ConfigFactory configFactory = mock(ConfigFactory.class);
      // FIXME: Having to use proper config object rather than mock
      Config config =
          JaxbUtil.unmarshal(getClass().getResourceAsStream("/sample.json"), Config.class);
      when(configFactory.getConfig()).thenReturn(config);
      staticConfigFactory.when(ConfigFactory::create).thenReturn(configFactory);

      Enclave enclave = EnclaveProvider.provider();

      assertThat(enclave).isNotNull();

      assertThat(enclave)
          .describedAs("Second call should return cached/held instance")
          .isSameAs(EnclaveProvider.provider());
    }
  }
}
