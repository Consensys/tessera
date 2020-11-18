package com.quorum.tessera.transaction.publish;

import org.junit.Test;

import java.util.Optional;
import java.util.ServiceLoader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class BatchPayloadPublisherTest {

    @Test
    public void create() {

        BatchPayloadPublisher expected = mock(BatchPayloadPublisher.class);

        BatchPayloadPublisher result;
        try(var serviceLoaderMockedStatic = mockStatic(ServiceLoader.class)) {

            ServiceLoader<BatchPayloadPublisher> serviceLoader = mock(ServiceLoader.class);
            when(serviceLoader.findFirst()).thenReturn(Optional.of(expected));
            serviceLoaderMockedStatic.when(() -> ServiceLoader.load(BatchPayloadPublisher.class))
                .thenReturn(serviceLoader);

            result = BatchPayloadPublisher.create();
            verify(serviceLoader).findFirst();
            verifyNoMoreInteractions(serviceLoader);
            serviceLoaderMockedStatic.verify(() -> ServiceLoader.load(BatchPayloadPublisher.class));
            serviceLoaderMockedStatic.verifyNoMoreInteractions();

        }

        assertThat(result).isSameAs(expected);

    }


}
