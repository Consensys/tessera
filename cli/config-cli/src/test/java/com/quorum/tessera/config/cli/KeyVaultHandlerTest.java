package com.quorum.tessera.config.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.ServiceLoader;
import org.junit.Test;

public class KeyVaultHandlerTest {

  @Test
  public void create() {
    ServiceLoader<KeyVaultHandler> serviceLoader = mock(ServiceLoader.class);
    KeyVaultHandler keyVaultHandler = mock(KeyVaultHandler.class);
    when(serviceLoader.findFirst()).thenReturn(Optional.of(keyVaultHandler));
    final KeyVaultHandler result;
    try (var serviceLoaderMockedStatic = mockStatic(ServiceLoader.class)) {
      serviceLoaderMockedStatic
          .when(() -> ServiceLoader.load(KeyVaultHandler.class))
          .thenReturn(serviceLoader);
      result = KeyVaultHandler.create();

      serviceLoaderMockedStatic.verify(() -> ServiceLoader.load(KeyVaultHandler.class));
      serviceLoaderMockedStatic.verifyNoMoreInteractions();
    }

    assertThat(result).isNotNull().isSameAs(keyVaultHandler);

    verify(serviceLoader).findFirst();
    verifyNoMoreInteractions(serviceLoader);
    verifyNoInteractions(keyVaultHandler);
  }
}
