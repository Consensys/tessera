package com.quorum.tessera.transaction.publish;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import org.junit.Test;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.*;

public class PayloadPublisherFactoryTest {

//    private MockedStatic<PayloadPublisherFactory> staticPayloadPublisherFactory;
//
//    private ServiceLoader<PayloadPublisherFactory> serviceLoader;
//
//
//    @Before
//    public void beforeTest() {
//        staticPayloadPublisherFactory = mockStatic(PayloadPublisherFactory.class);
//        serviceLoader = mock(ServiceLoader.class);
//
//
//    }
//
//    @After
//    public void afterTest() {
//        try {
//            staticPayloadPublisherFactory.verifyNoMoreInteractions();
//            verifyNoMoreInteractions(serviceLoader);
//        } finally {
//            staticPayloadPublisherFactory.close();
//        }
//
//    }

    @Test
    public void createFactoryAndThenPublisher() {

        final Config config = new Config();
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setApp(AppType.P2P);
        serverConfig.setCommunicationType(CommunicationType.REST);
        config.setServerConfigs(List.of(serverConfig));

        PayloadPublisherFactory expected = mock(PayloadPublisherFactory.class);
        when(expected.communicationType()).thenReturn(CommunicationType.REST);

        PayloadPublisherFactory actual;
        try (var staticServiceLoader = mockStatic(ServiceLoader.class)) {
            ServiceLoader<PayloadPublisherFactory> serviceLoader = mock(ServiceLoader.class);

            staticServiceLoader.when(() -> ServiceLoader.load(PayloadPublisherFactory.class))
                .thenReturn(serviceLoader);


            ServiceLoader.Provider<PayloadPublisherFactory> provider = mock(ServiceLoader.Provider.class);
            when(provider.get()).thenReturn(expected);

            when(serviceLoader.stream()).thenReturn(Stream.of(provider));

            actual = PayloadPublisherFactory.newFactory(config);

            staticServiceLoader.verify(() -> ServiceLoader.load(PayloadPublisherFactory.class));
            verify(serviceLoader).stream();
            verify(provider).get();
            verifyNoMoreInteractions(serviceLoader,provider);

            staticServiceLoader.verifyNoMoreInteractions();
        }

        assertThat(actual).isSameAs(expected);

    }

    @Test
    public void createFactoryAndThenPublisherNoFactoryFound() {

        final Config config = new Config();
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setApp(AppType.P2P);
        serverConfig.setCommunicationType(CommunicationType.WEB_SOCKET);
        config.setServerConfigs(List.of(serverConfig));

        PayloadPublisherFactory expected = mock(PayloadPublisherFactory.class);
        when(expected.communicationType()).thenReturn(CommunicationType.REST);

        try (var staticServiceLoader = mockStatic(ServiceLoader.class)) {
            ServiceLoader<PayloadPublisherFactory> serviceLoader = mock(ServiceLoader.class);
            staticServiceLoader.when(() -> ServiceLoader.load(PayloadPublisherFactory.class))
                .thenReturn(serviceLoader);

            ServiceLoader.Provider<PayloadPublisherFactory> provider = mock(ServiceLoader.Provider.class);
            when(provider.get()).thenReturn(expected);

            when(serviceLoader.stream()).thenReturn(Stream.of(provider));

            try {
                PayloadPublisherFactory.newFactory(config);
                failBecauseExceptionWasNotThrown(UnsupportedOperationException.class);
            } catch (UnsupportedOperationException ex) {
                staticServiceLoader.verify(() -> ServiceLoader.load(PayloadPublisherFactory.class));
                verify(serviceLoader).stream();
                verify(provider).get();
                verifyNoMoreInteractions(serviceLoader,provider);

                staticServiceLoader.verifyNoMoreInteractions();
            }

        }
    }
}
