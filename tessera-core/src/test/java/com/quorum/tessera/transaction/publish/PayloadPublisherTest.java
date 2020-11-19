package com.quorum.tessera.transaction.publish;

import org.junit.Test;

import java.util.ServiceLoader;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.Mockito.*;

public class PayloadPublisherTest {

    @Test
    public void create() {

        PayloadPublisher expected = mock(PayloadPublisher.class);

        PayloadPublisher result;
        try(
            var serviceLoaderMockedStatic = mockStatic(ServiceLoader.class)) {

            ServiceLoader<PayloadPublisher> serviceLoader = mock(ServiceLoader.class);
            ServiceLoader.Provider<PayloadPublisher> payloadPublisherProvider = mock(ServiceLoader.Provider.class);
            when(payloadPublisherProvider.get()).thenReturn(expected);
            when(serviceLoader.stream()).thenReturn(Stream.of(payloadPublisherProvider));

            serviceLoaderMockedStatic.when(() -> ServiceLoader.load(PayloadPublisher.class))
                .thenReturn(serviceLoader);

            result = PayloadPublisher.create();

            verify(serviceLoader).stream();
            verifyNoMoreInteractions(serviceLoader);

            serviceLoaderMockedStatic.verify(() -> ServiceLoader.load(PayloadPublisher.class));
            serviceLoaderMockedStatic.verifyNoMoreInteractions();

        }
        assertThat(result).isSameAs(expected);
    }

    @Test
    public void ambigiousResults() {

        Throwable result;
        try(
            var serviceLoaderMockedStatic = mockStatic(ServiceLoader.class)) {

            ServiceLoader<PayloadPublisher> serviceLoader = mock(ServiceLoader.class);

            ServiceLoader.Provider<PayloadPublisher> payloadPublisherProvider = mock(ServiceLoader.Provider.class);
            ServiceLoader.Provider<PayloadPublisher> anotherPayloadPublisherProvider = mock(ServiceLoader.Provider.class);

            when(serviceLoader.stream()).thenReturn(Stream.of(payloadPublisherProvider,anotherPayloadPublisherProvider));

            serviceLoaderMockedStatic.when(() -> ServiceLoader.load(PayloadPublisher.class)).thenReturn(serviceLoader);

            result = catchThrowable(() -> PayloadPublisher.create());

            verify(serviceLoader).stream();
            verifyNoMoreInteractions(serviceLoader);

            serviceLoaderMockedStatic.verify(() -> ServiceLoader.load(PayloadPublisher.class));
            serviceLoaderMockedStatic.verifyNoMoreInteractions();

        }
        assertThat(result)
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Ambiguous ServiceLoader lookup found multiple PayloadPublisher instances.");


    }



}
