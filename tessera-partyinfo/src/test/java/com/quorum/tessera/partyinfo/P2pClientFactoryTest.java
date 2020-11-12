package com.quorum.tessera.partyinfo;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import org.junit.Test;

import java.util.NoSuchElementException;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

public class P2pClientFactoryTest {

    @Test(expected = NoSuchElementException.class)
    public void newFactoryNullCommuicationType() {

        try(var staticServliceLoader = mockStatic(ServiceLoader.class)) {

            P2pClientFactory mockP2pClientFactory = mock(P2pClientFactory.class);
            when(mockP2pClientFactory.communicationType()).thenReturn(CommunicationType.WEB_SOCKET);

            ServiceLoader serviceLoader = mock(ServiceLoader.class);
            ServiceLoader.Provider serviceLoaderProvider = mock(ServiceLoader.Provider.class);
            when(serviceLoaderProvider.get()).thenReturn(mockP2pClientFactory);
            when(serviceLoader.stream()).thenReturn(Stream.of(serviceLoaderProvider));

            staticServliceLoader.when(() -> ServiceLoader.load(P2pClientFactory.class))
                .thenReturn(serviceLoader);

            Config config = mock(Config.class);
            ServerConfig serverConfig = mock(ServerConfig.class);
            when(config.getP2PServerConfig()).thenReturn(serverConfig);
            when(serverConfig.getCommunicationType()).thenReturn(CommunicationType.REST);

            P2pClientFactory.newFactory(config);

            verify(serviceLoader).stream();
            verify(mockP2pClientFactory).communicationType();
            staticServliceLoader.verify(() -> ServiceLoader.load(P2pClientFactory.class));
            staticServliceLoader.verifyNoMoreInteractions();
            verifyNoMoreInteractions(serviceLoader);
            verifyNoMoreInteractions(mockP2pClientFactory);
        }
    }

    @Test
    public void createRestClientFactory() {

        try(var staticServliceLoader = mockStatic(ServiceLoader.class)) {

            P2pClientFactory mockP2pClientFactory = mock(P2pClientFactory.class);
            when(mockP2pClientFactory.communicationType()).thenReturn(CommunicationType.REST);

            ServiceLoader serviceLoader = mock(ServiceLoader.class);
            ServiceLoader.Provider serviceLoaderProvider = mock(ServiceLoader.Provider.class);
            when(serviceLoaderProvider.get()).thenReturn(mockP2pClientFactory);
            when(serviceLoader.stream()).thenReturn(Stream.of(serviceLoaderProvider));

            staticServliceLoader.when(() -> ServiceLoader.load(P2pClientFactory.class))
                .thenReturn(serviceLoader);

            Config config = mock(Config.class);
            ServerConfig serverConfig = mock(ServerConfig.class);
            when(config.getP2PServerConfig()).thenReturn(serverConfig);
            when(serverConfig.getCommunicationType()).thenReturn(CommunicationType.REST);

            P2pClientFactory.newFactory(config);

            verify(serviceLoader).stream();
            verify(mockP2pClientFactory).communicationType();
            staticServliceLoader.verify(() -> ServiceLoader.load(P2pClientFactory.class));
            staticServliceLoader.verifyNoMoreInteractions();
            verifyNoMoreInteractions(serviceLoader);
            verifyNoMoreInteractions(mockP2pClientFactory);
        }

    }
}
