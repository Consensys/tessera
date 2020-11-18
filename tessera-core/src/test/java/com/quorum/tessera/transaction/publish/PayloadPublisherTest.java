package com.quorum.tessera.transaction.publish;

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
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class PayloadPublisherTest {

    @Test
    public void create() {

        ConfigFactory configFactory = mock(ConfigFactory.class);
        Config config = mock(Config.class);
        ServerConfig serverConfig = mock(ServerConfig.class);
        when(serverConfig.getCommunicationType()).thenReturn(CommunicationType.REST);
        when(config.getP2PServerConfig()).thenReturn(serverConfig);
        when(configFactory.getConfig()).thenReturn(config);

        PayloadPublisher expected = mock(PayloadPublisher.class);
        when(expected.communicationType()).thenReturn(CommunicationType.REST);

        PayloadPublisher result;
        try(
            var configFactoryMockedStatic= mockStatic(ConfigFactory.class);
            var serviceLoaderMockedStatic = mockStatic(ServiceLoader.class)) {

            configFactoryMockedStatic.when(ConfigFactory::create).thenReturn(configFactory);

            ServiceLoader<PayloadPublisher> serviceLoader = mock(ServiceLoader.class);
            ServiceLoader.Provider<PayloadPublisher> payloadPublisherProvider = mock(ServiceLoader.Provider.class);
            when(payloadPublisherProvider.get()).thenReturn(expected);
            when(serviceLoader.stream()).thenReturn(Stream.of(payloadPublisherProvider));

            serviceLoaderMockedStatic.when(() -> ServiceLoader.load(PayloadPublisher.class))
                .thenReturn(serviceLoader);

            result = PayloadPublisher.create();

            verify(serviceLoader).stream();
            verifyNoMoreInteractions(serviceLoader);
            verify(configFactory).getConfig();
            verifyNoMoreInteractions(configFactory);

            configFactoryMockedStatic.verify(ConfigFactory::create);
            configFactoryMockedStatic.verifyNoMoreInteractions();

            serviceLoaderMockedStatic.verify(() -> ServiceLoader.load(PayloadPublisher.class));
            serviceLoaderMockedStatic.verifyNoMoreInteractions();

        }
        assertThat(result).isSameAs(expected);

    }

    @Test
    public void createNoMatch() {

        ConfigFactory configFactory = mock(ConfigFactory.class);
        Config config = mock(Config.class);
        ServerConfig serverConfig = mock(ServerConfig.class);
        when(serverConfig.getCommunicationType()).thenReturn(CommunicationType.WEB_SOCKET);
        when(config.getP2PServerConfig()).thenReturn(serverConfig);
        when(configFactory.getConfig()).thenReturn(config);

        PayloadPublisher expected = mock(PayloadPublisher.class);
        when(expected.communicationType()).thenReturn(CommunicationType.REST);

        Throwable result;
        try(
            var configFactoryMockedStatic= mockStatic(ConfigFactory.class);
            var serviceLoaderMockedStatic = mockStatic(ServiceLoader.class)) {

            configFactoryMockedStatic.when(ConfigFactory::create).thenReturn(configFactory);

            ServiceLoader<PayloadPublisher> serviceLoader = mock(ServiceLoader.class);
            ServiceLoader.Provider<PayloadPublisher> payloadPublisherProvider = mock(ServiceLoader.Provider.class);
            when(payloadPublisherProvider.get()).thenReturn(expected);
            when(serviceLoader.stream()).thenReturn(Stream.of(payloadPublisherProvider));

            serviceLoaderMockedStatic.when(() -> ServiceLoader.load(PayloadPublisher.class))
                .thenReturn(serviceLoader);

            result = catchThrowable(() -> PayloadPublisher.create());


            verify(serviceLoader).stream();
            verifyNoMoreInteractions(serviceLoader);
            verify(configFactory).getConfig();
            verifyNoMoreInteractions(configFactory);

            configFactoryMockedStatic.verify(ConfigFactory::create);
            configFactoryMockedStatic.verifyNoMoreInteractions();

            serviceLoaderMockedStatic.verify(() -> ServiceLoader.load(PayloadPublisher.class));
            serviceLoaderMockedStatic.verifyNoMoreInteractions();

        }
        assertThat(result).isExactlyInstanceOf(NoSuchElementException.class);

    }
}
