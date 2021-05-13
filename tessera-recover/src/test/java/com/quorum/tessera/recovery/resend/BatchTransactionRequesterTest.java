package com.quorum.tessera.recovery.resend;

import static org.mockito.Mockito.*;

import com.quorum.tessera.serviceloader.ServiceLoaderUtil;
import java.util.ServiceLoader;
import org.junit.Test;

public class BatchTransactionRequesterTest {

  @Test
  public void create() {
    try (var serviceLoaderUtilMockedStatic = mockStatic(ServiceLoaderUtil.class);
        var serviceLoaderMockedStatic = mockStatic(ServiceLoader.class)) {

      ServiceLoader<BatchTransactionRequester> serviceLoader = mock(ServiceLoader.class);
      serviceLoaderMockedStatic
          .when(() -> ServiceLoader.load(BatchTransactionRequester.class))
          .thenReturn(serviceLoader);

      BatchTransactionRequester.create();

      serviceLoaderUtilMockedStatic.verify(() -> ServiceLoaderUtil.loadSingle(serviceLoader));
      serviceLoaderUtilMockedStatic.verifyNoMoreInteractions();

      serviceLoaderMockedStatic.verify(() -> ServiceLoader.load(BatchTransactionRequester.class));
      serviceLoaderMockedStatic.verifyNoMoreInteractions();
      verifyNoInteractions(serviceLoader);
    }
  }
}
