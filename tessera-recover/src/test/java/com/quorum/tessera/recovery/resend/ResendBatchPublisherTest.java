package com.quorum.tessera.recovery.resend;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.config.ServerConfig;
import org.junit.Test;

import java.util.ServiceLoader;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ResendBatchPublisherTest {


    @Test
    public void createNoResults() {
        Config config = mock(Config.class);
        ServerConfig serverConfig = mock(ServerConfig.class);
        when(serverConfig.getCommunicationType()).thenReturn(CommunicationType.REST);
        when(config.getP2PServerConfig()).thenReturn(serverConfig);

        ConfigFactory configFactory = mock(ConfigFactory.class);
        when(configFactory.getConfig()).thenReturn(config);

        try(var staticServiceLoader = mockStatic(ServiceLoader.class);
            var staticConfigFactory = mockStatic(ConfigFactory.class)
        ) {

            staticConfigFactory.when(ConfigFactory::create).thenReturn(configFactory);

            ServiceLoader<ResendBatchPublisher> serviceLoader = mock(ServiceLoader.class);
            ServiceLoader.Provider<ResendBatchPublisher> provider = mock(ServiceLoader.Provider.class);

            ResendBatchPublisher resendBatchPublisher = mock(ResendBatchPublisher.class);
            when(resendBatchPublisher.communicationType()).thenReturn(CommunicationType.WEB_SOCKET);
            when(provider.get()).thenReturn(mock(ResendBatchPublisher.class));
            when(serviceLoader.stream()).thenReturn(Stream.of(
                provider
            ));

            staticServiceLoader.when(() -> ServiceLoader.load(ResendBatchPublisher.class)).thenReturn(serviceLoader);

            try {
                ResendBatchPublisher.create();
                failBecauseExceptionWasNotThrown(UnsupportedOperationException.class);
            } catch (UnsupportedOperationException ex) {
                staticConfigFactory.verify(ConfigFactory::create);
                staticConfigFactory.verifyNoMoreInteractions();

                verify(configFactory).getConfig();
                verifyNoMoreInteractions(configFactory);
            }
        }
    }

    @Test
    public void create() {

        Config config = mock(Config.class);
        ServerConfig serverConfig = mock(ServerConfig.class);
        when(serverConfig.getCommunicationType()).thenReturn(CommunicationType.REST);
        when(config.getP2PServerConfig()).thenReturn(serverConfig);

        ResendBatchPublisher resendBatchPublisher = mock(ResendBatchPublisher.class);
        when(resendBatchPublisher.communicationType()).thenReturn(CommunicationType.REST);

        ConfigFactory configFactory = mock(ConfigFactory.class);
        when(configFactory.getConfig()).thenReturn(config);

        ResendBatchPublisher result;//weird thing where needs to be outside braces
        try(var staticServiceLoader = mockStatic(ServiceLoader.class);
            var staticConfigFactory = mockStatic(ConfigFactory.class)
        ) {

            staticConfigFactory.when(ConfigFactory::create).thenReturn(configFactory);

            ServiceLoader<ResendBatchPublisher> serviceLoader = mock(ServiceLoader.class);
            ServiceLoader.Provider<ResendBatchPublisher> provider = mock(ServiceLoader.Provider.class);
            when(provider.get()).thenReturn(resendBatchPublisher);
            when(serviceLoader.stream()).thenReturn(Stream.of(provider));

            staticServiceLoader.when(() -> ServiceLoader.load(ResendBatchPublisher.class)).thenReturn(serviceLoader);

            result = ResendBatchPublisher.create();

            staticConfigFactory.verify(ConfigFactory::create);
            staticConfigFactory.verifyNoMoreInteractions();

            verify(configFactory).getConfig();
            verifyNoMoreInteractions(configFactory);
        }

        assertThat(result).isNotNull()
            .isSameAs(resendBatchPublisher);

        verify(config).getP2PServerConfig();
        verifyNoMoreInteractions(config);

        verify(serverConfig).getCommunicationType();
        verifyNoMoreInteractions(serverConfig);

    }



}
