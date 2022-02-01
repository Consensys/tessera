package com.quorum.tessera.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.ServiceLoader;
import org.junit.Test;

public class InboxTest {

  @Test
  public void testCreate() {
    Inbox inbox = mock(Inbox.class);
    ServiceLoader<Inbox> serviceLoader = mock(ServiceLoader.class);
    when(serviceLoader.findFirst()).thenReturn(Optional.of(inbox));

    Inbox result;
    var serviceLoaderMockedStatic = mockStatic(ServiceLoader.class);
    try {
      serviceLoaderMockedStatic
          .when(() -> ServiceLoader.load(Inbox.class))
          .thenReturn(serviceLoader);

      result = Inbox.create();

      serviceLoaderMockedStatic.verify(() -> ServiceLoader.load(Inbox.class));
      serviceLoaderMockedStatic.verifyNoMoreInteractions();
    } finally {
      serviceLoaderMockedStatic.close();
    }
    verify(serviceLoader).findFirst();
    verifyNoMoreInteractions(serviceLoader);
    assertThat(result).isNotNull().isSameAs(inbox);
  }
}
