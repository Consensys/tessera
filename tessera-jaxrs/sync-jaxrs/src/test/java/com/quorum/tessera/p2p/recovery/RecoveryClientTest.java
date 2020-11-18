package com.quorum.tessera.p2p.recovery;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.config.ServerConfig;
import org.junit.Test;

import java.util.NoSuchElementException;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

public class RecoveryClientTest {

    @Test
    public void create() {
        RecoveryClient expected = mock(RecoveryClient.class);
        when(expected.communicationType()).thenReturn(CommunicationType.REST);

        RecoveryClient result;
        try(var configFactoryMockedStatic = mockStatic(ConfigFactory.class);
            var serviceLoaderMockedStatic = mockStatic(ServiceLoader.class)
        ) {

            ServiceLoader<RecoveryClient> serviceLoader = mock(ServiceLoader.class);
            ServiceLoader.Provider<RecoveryClient> provider = mock(ServiceLoader.Provider.class);
            when(provider.get()).thenReturn(expected);
            when(serviceLoader.stream()).thenReturn(Stream.of(provider));
            serviceLoaderMockedStatic.when(() -> ServiceLoader.load(RecoveryClient.class)).thenReturn(serviceLoader);


            ConfigFactory configFactory = mock(ConfigFactory.class);
            Config config = mock(Config.class);
            ServerConfig serverConfig = mock(ServerConfig.class);
            when(serverConfig.getCommunicationType()).thenReturn(CommunicationType.REST);
            when(config.getP2PServerConfig()).thenReturn(serverConfig);
            when(configFactory.getConfig()).thenReturn(config);
            configFactoryMockedStatic.when(ConfigFactory::create).thenReturn(configFactory);

            result = RecoveryClient.create();

            verify(serviceLoader).stream();
            verify(provider).get();

            verifyNoMoreInteractions(serviceLoader);
            verifyNoMoreInteractions(provider);


            serviceLoaderMockedStatic.verify(() -> ServiceLoader.load(RecoveryClient.class));
            serviceLoaderMockedStatic.verifyNoMoreInteractions();

            configFactoryMockedStatic.verify(ConfigFactory::create);
            configFactoryMockedStatic.verifyNoMoreInteractions();
        }

        assertThat(result).isSameAs(expected);


    }

    @Test
    public void createNoMatch() {
        RecoveryClient expected = mock(RecoveryClient.class);
        when(expected.communicationType()).thenReturn(CommunicationType.WEB_SOCKET);

        Throwable result;
        try(var configFactoryMockedStatic = mockStatic(ConfigFactory.class);
            var serviceLoaderMockedStatic = mockStatic(ServiceLoader.class)
        ) {

            ServiceLoader<RecoveryClient> serviceLoader = mock(ServiceLoader.class);
            ServiceLoader.Provider<RecoveryClient> provider = mock(ServiceLoader.Provider.class);
            when(provider.get()).thenReturn(expected);
            when(serviceLoader.stream()).thenReturn(Stream.of(provider));
            serviceLoaderMockedStatic.when(() -> ServiceLoader.load(RecoveryClient.class)).thenReturn(serviceLoader);


            ConfigFactory configFactory = mock(ConfigFactory.class);
            Config config = mock(Config.class);
            ServerConfig serverConfig = mock(ServerConfig.class);
            when(serverConfig.getCommunicationType()).thenReturn(CommunicationType.REST);
            when(config.getP2PServerConfig()).thenReturn(serverConfig);
            when(configFactory.getConfig()).thenReturn(config);
            configFactoryMockedStatic.when(ConfigFactory::create).thenReturn(configFactory);

            result = catchThrowable(() -> RecoveryClient.create());

            verify(serviceLoader).stream();
            verify(provider).get();

            verifyNoMoreInteractions(serviceLoader);
            verifyNoMoreInteractions(provider);

            serviceLoaderMockedStatic.verify(() -> ServiceLoader.load(RecoveryClient.class));
            serviceLoaderMockedStatic.verifyNoMoreInteractions();

            configFactoryMockedStatic.verify(ConfigFactory::create);
            configFactoryMockedStatic.verifyNoMoreInteractions();
        }

        assertThat(result).isExactlyInstanceOf(NoSuchElementException.class);


    }

}
