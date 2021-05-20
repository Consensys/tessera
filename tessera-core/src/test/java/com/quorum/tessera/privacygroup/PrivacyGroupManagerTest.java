package com.quorum.tessera.privacygroup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.ServiceLoader;
import org.junit.Test;

public class PrivacyGroupManagerTest {
  @Test
  public void create() {
    PrivacyGroupManager privacyGroupManager = mock(PrivacyGroupManager.class);
    ServiceLoader<PrivacyGroupManager> serviceLoader = mock(ServiceLoader.class);
    when(serviceLoader.findFirst()).thenReturn(Optional.of(privacyGroupManager));
    PrivacyGroupManager result;
    try (var serviceLoaderMockedStatic = mockStatic(ServiceLoader.class)) {

      serviceLoaderMockedStatic
          .when(() -> ServiceLoader.load(PrivacyGroupManager.class))
          .thenReturn(serviceLoader);

      result = PrivacyGroupManager.create();

      serviceLoaderMockedStatic.verify(() -> ServiceLoader.load(PrivacyGroupManager.class));
      serviceLoaderMockedStatic.verifyNoMoreInteractions();
    }
    verify(serviceLoader).findFirst();
    verifyNoMoreInteractions(serviceLoader);
    assertThat(result).isNotNull().isSameAs(privacyGroupManager);
  }
}
