package com.quorum.tessera.privacygroup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.junit.Test;

public class ResidentGroupHandlerProviderTest {

  @Test
  public void provider() {

    PrivacyGroupManager privacyGroupManager = mock(PrivacyGroupManager.class);
    try (var privacyGroupManagerMockedStatic = mockStatic(PrivacyGroupManager.class)) {
      privacyGroupManagerMockedStatic
          .when(PrivacyGroupManager::create)
          .thenReturn(privacyGroupManager);
      ResidentGroupHandler result = ResidentGroupHandlerProvider.provider();
      assertThat(result).isNotNull();
      privacyGroupManagerMockedStatic.verify(PrivacyGroupManager::create);
      privacyGroupManagerMockedStatic.verifyNoMoreInteractions();
      verifyNoInteractions(privacyGroupManager);
    }
  }

  @Test
  public void defaultConstructorForCoverage() {
    assertThat(new ResidentGroupHandlerProvider()).isNotNull();
  }
}
