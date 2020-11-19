package com.quorum.tessera.partyinfo;

import com.quorum.tessera.serviceloader.ServiceLoaderUtil;
import org.junit.Test;

import java.util.ServiceLoader;

import static org.mockito.Mockito.*;

public class P2pClientTest {

    @Test
    public void create() {
        try(
            var serviceLoaderUtilMockedStatic = mockStatic(ServiceLoaderUtil.class);
            var serviceLoaderMockedStatic = mockStatic(ServiceLoader.class)
        ) {

            ServiceLoader<P2pClient> serviceLoader = mock(ServiceLoader.class);
            serviceLoaderMockedStatic.when(() -> ServiceLoader.load(P2pClient.class)).thenReturn(serviceLoader);

            P2pClient.create();

            serviceLoaderUtilMockedStatic.verify(() -> ServiceLoaderUtil.loadSingle(serviceLoader));
            serviceLoaderUtilMockedStatic.verifyNoMoreInteractions();

            serviceLoaderMockedStatic.verify(() -> ServiceLoader.load(P2pClient.class));
            serviceLoaderMockedStatic.verifyNoMoreInteractions();
            verifyNoInteractions(serviceLoader);
        }
    }
}
