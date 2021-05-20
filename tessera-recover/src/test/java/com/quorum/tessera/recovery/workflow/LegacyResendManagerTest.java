package com.quorum.tessera.recovery.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.quorum.tessera.serviceloader.ServiceLoaderUtil;
import java.util.ServiceLoader;
import org.junit.Test;

public class LegacyResendManagerTest {
  @Test
  public void createReturnsInstance() {

    LegacyResendManager legacyResendManager = mock(LegacyResendManager.class);
    final LegacyResendManager result;
    try (var serviceLoaderUtilMockStatic = mockStatic(ServiceLoaderUtil.class)) {
      serviceLoaderUtilMockStatic
          .when(() -> ServiceLoaderUtil.loadSingle(any(ServiceLoader.class)))
          .thenReturn(legacyResendManager);

      result = LegacyResendManager.create();

      serviceLoaderUtilMockStatic.verify(
          () -> ServiceLoaderUtil.loadSingle(any(ServiceLoader.class)));
      serviceLoaderUtilMockStatic.verifyNoMoreInteractions();
    }

    assertThat(result).isNotNull().isSameAs(legacyResendManager);
  }
}
