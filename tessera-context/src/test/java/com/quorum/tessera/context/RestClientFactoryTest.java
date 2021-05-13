package com.quorum.tessera.context;

import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.ServiceLoader;
import org.junit.Test;
import org.mockito.MockedStatic;

public class RestClientFactoryTest {
  @Test
  public void create() {

    try (MockedStatic<ServiceLoader> mockedStatic = mockStatic(ServiceLoader.class)) {

      RestClientFactory clientFactory = mock(RestClientFactory.class);

      ServiceLoader serviceLoader = mock(ServiceLoader.class);
      doReturn(Optional.of(clientFactory)).when(serviceLoader).findFirst();

      mockedStatic
          .when(() -> ServiceLoader.load(RestClientFactory.class))
          .thenReturn(serviceLoader);

      RestClientFactory.create();
      verify(serviceLoader).findFirst();

      mockedStatic.verify(() -> ServiceLoader.load(RestClientFactory.class));
    }
  }
}
