package com.quorum.tessera.recovery;

import static org.mockito.Mockito.*;

import com.quorum.tessera.serviceloader.ServiceLoaderUtil;
import java.util.ServiceLoader;
import org.junit.Test;

public class RecoveryTest {
  @Test
  public void create() {
    try (var serviceLoaderUtilMockedStatic = mockStatic(ServiceLoaderUtil.class);
        var serviceLoaderMockedStatic = mockStatic(ServiceLoader.class)) {

      ServiceLoader<Recovery> serviceLoader = mock(ServiceLoader.class);
      serviceLoaderMockedStatic
          .when(() -> ServiceLoader.load(Recovery.class))
          .thenReturn(serviceLoader);

      Recovery.create();

      serviceLoaderUtilMockedStatic.verify(() -> ServiceLoaderUtil.loadSingle(serviceLoader));
      serviceLoaderUtilMockedStatic.verifyNoMoreInteractions();

      serviceLoaderMockedStatic.verify(() -> ServiceLoader.load(Recovery.class));
      serviceLoaderMockedStatic.verifyNoMoreInteractions();
      verifyNoInteractions(serviceLoader);
    }
  }
}
