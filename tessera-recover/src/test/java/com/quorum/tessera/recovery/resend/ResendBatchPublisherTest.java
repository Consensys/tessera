package com.quorum.tessera.recovery.resend;

import static org.mockito.Mockito.*;

import com.quorum.tessera.serviceloader.ServiceLoaderUtil;
import java.util.ServiceLoader;
import org.junit.Test;

public class ResendBatchPublisherTest {

  @Test
  public void create() {
    try (var serviceLoaderUtilMockedStatic = mockStatic(ServiceLoaderUtil.class);
        var serviceLoaderMockedStatic = mockStatic(ServiceLoader.class)) {

      ServiceLoader<ResendBatchPublisher> serviceLoader = mock(ServiceLoader.class);
      serviceLoaderMockedStatic
          .when(() -> ServiceLoader.load(ResendBatchPublisher.class))
          .thenReturn(serviceLoader);

      ResendBatchPublisher.create();

      serviceLoaderUtilMockedStatic.verify(() -> ServiceLoaderUtil.loadSingle(serviceLoader));
      serviceLoaderUtilMockedStatic.verifyNoMoreInteractions();

      serviceLoaderMockedStatic.verify(() -> ServiceLoader.load(ResendBatchPublisher.class));
      serviceLoaderMockedStatic.verifyNoMoreInteractions();
      verifyNoInteractions(serviceLoader);
    }
  }
}
