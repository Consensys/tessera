package com.quorum.tessera.key.vault;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

import com.quorum.tessera.config.KeyVaultType;
import java.util.ServiceLoader;
import java.util.stream.Stream;
import org.junit.Test;

public class KeyVaultServiceFactoryTest {

  @Test
  public void getInstance() {

    for (KeyVaultType keyVaultType : KeyVaultType.values()) {

      KeyVaultServiceFactory otherKeyVaultServiceFactory = mock(KeyVaultServiceFactory.class);
      when(otherKeyVaultServiceFactory.getType())
          .thenReturn(
              Stream.of(KeyVaultType.values()).filter(k -> k != keyVaultType).findAny().get());

      KeyVaultServiceFactory expected = mock(KeyVaultServiceFactory.class);
      when(expected.getType()).thenReturn(keyVaultType);

      KeyVaultServiceFactory keyVaultServiceFactory;
      try (var mockedStaticServiceLoader = mockStatic(ServiceLoader.class)) {

        ServiceLoader<KeyVaultServiceFactory> serviceLoader = mock(ServiceLoader.class);
        ServiceLoader.Provider<KeyVaultServiceFactory> provider =
            mock(ServiceLoader.Provider.class);
        when(provider.get()).thenReturn(expected);

        ServiceLoader.Provider<KeyVaultServiceFactory> otherProvider =
            mock(ServiceLoader.Provider.class);
        when(otherProvider.get()).thenReturn(otherKeyVaultServiceFactory);

        when(serviceLoader.stream()).thenReturn(Stream.of(provider, otherProvider).unordered());

        mockedStaticServiceLoader
            .when(() -> ServiceLoader.load(KeyVaultServiceFactory.class))
            .thenReturn(serviceLoader);

        keyVaultServiceFactory = KeyVaultServiceFactory.getInstance(keyVaultType);

        verify(serviceLoader).stream();
        verifyNoMoreInteractions(serviceLoader);

        verify(provider).get();
        verifyNoMoreInteractions(provider);

        mockedStaticServiceLoader.verify(() -> ServiceLoader.load(KeyVaultServiceFactory.class));
        mockedStaticServiceLoader.verifyNoMoreInteractions();
      }

      assertThat(keyVaultServiceFactory).isSameAs(expected);
    }
  }

  @Test(expected = NoKeyVaultServiceFactoryException.class)
  public void instanceNotFound() {
    try (var mockedStaticServiceLoader = mockStatic(ServiceLoader.class)) {
      ServiceLoader<KeyVaultServiceFactory> serviceLoader = mock(ServiceLoader.class);
      ServiceLoader.Provider<KeyVaultServiceFactory> provider = mock(ServiceLoader.Provider.class);
      when(provider.get()).thenReturn(mock(KeyVaultServiceFactory.class));
      when(serviceLoader.stream()).thenReturn(Stream.of(provider));

      mockedStaticServiceLoader
          .when(() -> ServiceLoader.load(KeyVaultServiceFactory.class))
          .thenReturn(serviceLoader);

      KeyVaultServiceFactory.getInstance(KeyVaultType.AZURE);
    }
  }
}
