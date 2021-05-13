package com.quorum.tessera.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.ServiceLoader;
import org.junit.Test;

public class EncodedPayloadManagerTest {

  @Test
  public void create() {
    EncodedPayloadManager expected = mock(EncodedPayloadManager.class);
    EncodedPayloadManager encodedPayloadManager;
    try (var serviceLoaderMockedStatic = mockStatic(ServiceLoader.class)) {

      ServiceLoader<EncodedPayloadManager> serviceLoader = mock(ServiceLoader.class);
      when(serviceLoader.findFirst()).thenReturn(Optional.of(expected));
      serviceLoaderMockedStatic
          .when(() -> ServiceLoader.load(EncodedPayloadManager.class))
          .thenReturn(serviceLoader);

      encodedPayloadManager = EncodedPayloadManager.create();

      verify(serviceLoader).findFirst();
      verifyNoMoreInteractions(serviceLoader);

      serviceLoaderMockedStatic.verify(() -> ServiceLoader.load(EncodedPayloadManager.class));
      serviceLoaderMockedStatic.verifyNoMoreInteractions();
    }

    assertThat(encodedPayloadManager).isNotNull().isSameAs(expected);
  }
}
