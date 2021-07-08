package com.quorum.tessera.transaction.resend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Optional;
import java.util.ServiceLoader;
import org.junit.Test;

public class ResendManagerTest {
  @Test
  public void createFromServiceLoader() {

    ServiceLoader<ResendManager> serviceLoader = mock(ServiceLoader.class);

    ResendManager resendManager = mock(ResendManager.class);
    when(serviceLoader.findFirst()).thenReturn(Optional.of(resendManager));

    final ResendManager result;
    try (var serviceLoaderMockedStatic = mockStatic(ServiceLoader.class)) {

      serviceLoaderMockedStatic
          .when(() -> ServiceLoader.load(ResendManager.class))
          .thenReturn(serviceLoader);

      result = ResendManager.create();

      serviceLoaderMockedStatic.verify(() -> ServiceLoader.load(ResendManager.class));
      serviceLoaderMockedStatic.verifyNoMoreInteractions();
    }

    assertThat(result).isSameAs(resendManager);
    verify(serviceLoader).findFirst();
    verifyNoMoreInteractions(serviceLoader);
    verifyNoInteractions(resendManager);
  }
}
