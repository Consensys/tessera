package com.quorum.tessera.transaction.publish;

import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.Optional;
import java.util.ServiceLoader;

import static org.mockito.Mockito.*;

public class BatchPayloadPublisherFactoryTest {

    @Test
    public void newFactory() {

        try(MockedStatic<ServiceLoader> m = mockStatic(ServiceLoader.class)) {

            BatchPayloadPublisherFactory batchPayloadPublisherFactory = mock(BatchPayloadPublisherFactory.class);
            ServiceLoader serviceLoader = mock(ServiceLoader.class);
            when(serviceLoader.findFirst()).thenReturn(Optional.of(batchPayloadPublisherFactory));

            m.when(() -> ServiceLoader.load(BatchPayloadPublisherFactory.class)).thenReturn(serviceLoader);

            BatchPayloadPublisherFactory.newFactory();

            verify(serviceLoader).findFirst();
            m.verify(() -> ServiceLoader.load(BatchPayloadPublisherFactory.class));
            m.verifyNoMoreInteractions();

        }
    }

}
