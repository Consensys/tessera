package com.quorum.tessera.transaction.publish;

import com.quorum.tessera.serviceloader.ServiceLoaderUtil;
import org.junit.Test;

import java.util.ServiceLoader;

import static org.mockito.Mockito.*;

public class BatchPayloadPublisherTest {

    @Test
    public void create() {
        try(
            var serviceLoaderUtilMockedStatic = mockStatic(ServiceLoaderUtil.class);
            var serviceLoaderMockedStatic = mockStatic(ServiceLoader.class)
        ) {

            ServiceLoader<BatchPayloadPublisher> serviceLoader = mock(ServiceLoader.class);
            serviceLoaderMockedStatic.when(() -> ServiceLoader.load(BatchPayloadPublisher.class)).thenReturn(serviceLoader);

            BatchPayloadPublisher.create();

            serviceLoaderUtilMockedStatic.verify(() -> ServiceLoaderUtil.loadSingle(serviceLoader));
            serviceLoaderUtilMockedStatic.verifyNoMoreInteractions();

            serviceLoaderMockedStatic.verify(() -> ServiceLoader.load(BatchPayloadPublisher.class));
            serviceLoaderMockedStatic.verifyNoMoreInteractions();
            verifyNoInteractions(serviceLoader);
        }
    }

}
