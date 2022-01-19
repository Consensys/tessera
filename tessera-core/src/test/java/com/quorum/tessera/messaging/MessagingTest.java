package com.quorum.tessera.messaging;

import org.junit.Test;
import java.util.Optional;
import java.util.ServiceLoader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class MessagingTest {

  @Test
  public void testCreate() {
    Messaging messaging = mock(Messaging.class);
    ServiceLoader<Messaging> serviceLoader = mock(ServiceLoader.class);
    when(serviceLoader.findFirst()).thenReturn(Optional.of(messaging));

    Messaging result;
    try (var serviceLoaderMockedStatic = mockStatic(ServiceLoader.class)) {

      serviceLoaderMockedStatic
        .when(() -> ServiceLoader.load(Messaging.class))
        .thenReturn(serviceLoader);

      result = Messaging.create();

      serviceLoaderMockedStatic.verify(() -> ServiceLoader.load(Messaging.class));
      serviceLoaderMockedStatic.verifyNoMoreInteractions();
    }
    verify(serviceLoader).findFirst();
    verifyNoMoreInteractions(serviceLoader);
    assertThat(result).isNotNull().isSameAs(messaging);

  }
}
