package com.quorum.tessera.p2p.recovery;

import static org.mockito.Mockito.*;

import com.quorum.tessera.serviceloader.ServiceLoaderUtil;
import java.util.ServiceLoader;
import org.junit.Test;

public class RecoveryClientTest {

  @Test
  public void create() {
    try (var serviceLoaderUtilMockedStatic = mockStatic(ServiceLoaderUtil.class);
        var serviceLoaderMockedStatic = mockStatic(ServiceLoader.class)) {

      ServiceLoader<RecoveryClient> serviceLoader = mock(ServiceLoader.class);
      serviceLoaderMockedStatic
          .when(() -> ServiceLoader.load(RecoveryClient.class))
          .thenReturn(serviceLoader);

      RecoveryClient.create();

      serviceLoaderUtilMockedStatic.verify(() -> ServiceLoaderUtil.loadSingle(serviceLoader));
      serviceLoaderUtilMockedStatic.verifyNoMoreInteractions();

      serviceLoaderMockedStatic.verify(() -> ServiceLoader.load(RecoveryClient.class));
      serviceLoaderMockedStatic.verifyNoMoreInteractions();
      verifyNoInteractions(serviceLoader);
    }
  }
}
