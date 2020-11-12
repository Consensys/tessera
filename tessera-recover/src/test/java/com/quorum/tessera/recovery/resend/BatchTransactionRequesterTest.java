package com.quorum.tessera.recovery.resend;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.config.ServerConfig;
import org.junit.Test;

import java.util.NoSuchElementException;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class BatchTransactionRequesterTest {

    @Test
    public void create() {

        Config config = mock(Config.class);
        ServerConfig serverConfig = mock(ServerConfig.class);
        when(serverConfig.getCommunicationType()).thenReturn(CommunicationType.REST);
        when(config.getP2PServerConfig()).thenReturn(serverConfig);

        ConfigFactory configFactory = mock(ConfigFactory.class);
        when(configFactory.getConfig()).thenReturn(config);


        ServiceLoader<BatchTransactionRequester> serviceLoader = mock(ServiceLoader.class);
        ServiceLoader.Provider<BatchTransactionRequester> provider = mock(ServiceLoader.Provider.class);

        BatchTransactionRequester expected = mock(BatchTransactionRequester.class);
        when(expected.communicationType()).thenReturn(CommunicationType.REST);
        when(provider.get()).thenReturn(expected);
        when(serviceLoader.stream()).thenReturn(Stream.of(provider));

        BatchTransactionRequester result;
        try(
            var staticConfigFactory = mockStatic(ConfigFactory.class);
            var staticServiceLoader = mockStatic(ServiceLoader.class)
            ) {

            staticConfigFactory.when(ConfigFactory::create).thenReturn(configFactory);

            staticServiceLoader.when(() -> ServiceLoader.load(BatchTransactionRequester.class))
                .thenReturn(serviceLoader);

            result = BatchTransactionRequester.create();
        }
        assertThat(result).isNotNull().isSameAs(expected);
    }

    @Test(expected = NoSuchElementException.class)
    public void createNoMatch() {

        Config config = mock(Config.class);
        ServerConfig serverConfig = mock(ServerConfig.class);
        when(serverConfig.getCommunicationType()).thenReturn(CommunicationType.REST);
        when(config.getP2PServerConfig()).thenReturn(serverConfig);

        ConfigFactory configFactory = mock(ConfigFactory.class);
        when(configFactory.getConfig()).thenReturn(config);

        ServiceLoader<BatchTransactionRequester> serviceLoader = mock(ServiceLoader.class);
        ServiceLoader.Provider<BatchTransactionRequester> provider = mock(ServiceLoader.Provider.class);

        BatchTransactionRequester batchTransactionRequester = mock(BatchTransactionRequester.class);
        when(batchTransactionRequester.communicationType()).thenReturn(CommunicationType.WEB_SOCKET);
        when(provider.get()).thenReturn(batchTransactionRequester);
        when(serviceLoader.stream()).thenReturn(Stream.of(provider));

        try(
            var staticConfigFactory = mockStatic(ConfigFactory.class);
            var staticServiceLoader = mockStatic(ServiceLoader.class)
        ) {

            staticConfigFactory.when(ConfigFactory::create).thenReturn(configFactory);

            staticServiceLoader.when(() -> ServiceLoader.load(BatchTransactionRequester.class))
                .thenReturn(serviceLoader);

            BatchTransactionRequester.create();
        }
    }
}
