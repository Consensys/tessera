package com.quorum.tessera.enclave;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.ServiceLoader;
import org.junit.Test;

public class EnclaveClientFactoryTest {

  @Test
  public void create() throws Exception {

    EnclaveClientFactory expected = mock(EnclaveClientFactory.class);
    EnclaveClientFactory result;
    try (var mockStaticServiceLoader = mockStatic(ServiceLoader.class)) {

      ServiceLoader<EnclaveClientFactory> serviceLoader = mock(ServiceLoader.class);
      when(serviceLoader.findFirst()).thenReturn(Optional.of(expected));
      mockStaticServiceLoader
          .when(() -> ServiceLoader.load(EnclaveClientFactory.class))
          .thenReturn(serviceLoader);

      result = EnclaveClientFactory.create();
      mockStaticServiceLoader.verify(() -> ServiceLoader.load(EnclaveClientFactory.class));
      verify(serviceLoader).findFirst();
      mockStaticServiceLoader.verifyNoMoreInteractions();
      verifyNoMoreInteractions(serviceLoader);
    }
    assertThat(result).isSameAs(expected);
  }
}
