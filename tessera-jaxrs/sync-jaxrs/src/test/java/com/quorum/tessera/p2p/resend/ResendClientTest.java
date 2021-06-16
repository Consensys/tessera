package com.quorum.tessera.p2p.resend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.ServiceLoader;
import java.util.stream.Stream;
import org.junit.Test;

public class ResendClientTest {

  @Test
  public void create() {
    ResendClient expected = mock(ResendClient.class);
    ResendClient result;
    try (var serviceLoaderMockedStatic = mockStatic(ServiceLoader.class)) {

      ServiceLoader<ResendClient> serviceLoader = mock(ServiceLoader.class);
      ServiceLoader.Provider<ResendClient> provider = mock(ServiceLoader.Provider.class);
      when(provider.get()).thenReturn(expected);
      when(serviceLoader.stream()).thenReturn(Stream.of(provider));
      serviceLoaderMockedStatic
          .when(() -> ServiceLoader.load(ResendClient.class))
          .thenReturn(serviceLoader);

      result = ResendClient.create();

      verify(serviceLoader).stream();
      verify(provider).get();

      verifyNoMoreInteractions(serviceLoader);
      verifyNoMoreInteractions(provider);

      serviceLoaderMockedStatic.verify(() -> ServiceLoader.load(ResendClient.class));
      serviceLoaderMockedStatic.verifyNoMoreInteractions();
    }

    assertThat(result).isSameAs(expected);
  }
}
