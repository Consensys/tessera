package com.quorum.tessera.recovery.resend;

import com.quorum.tessera.serviceloader.ServiceLoaderUtil;
import org.junit.Test;

import java.util.ServiceLoader;

import static org.mockito.Mockito.*;

public class ResendBatchPublisherTest {

    @Test
    public void create() {
        try (
            var serviceLoaderUtilMockedStatic = mockStatic(ServiceLoaderUtil.class);
            var serviceLoaderMockedStatic = mockStatic(ServiceLoader.class)
        ) {

            ServiceLoader<ResendBatchPublisher> serviceLoader = mock(ServiceLoader.class);
            serviceLoaderMockedStatic.when(() -> ServiceLoader.load(ResendBatchPublisher.class)).thenReturn(serviceLoader);

            ResendBatchPublisher.create();

            serviceLoaderUtilMockedStatic.verify(() -> ServiceLoaderUtil.loadSingle(serviceLoader));
            serviceLoaderUtilMockedStatic.verifyNoMoreInteractions();

            serviceLoaderMockedStatic.verify(() -> ServiceLoader.load(ResendBatchPublisher.class));
            serviceLoaderMockedStatic.verifyNoMoreInteractions();
            verifyNoInteractions(serviceLoader);
        }
    }

}
