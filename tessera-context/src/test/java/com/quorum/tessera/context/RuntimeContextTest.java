package com.quorum.tessera.context;

import org.junit.Test;

import java.util.Optional;
import java.util.ServiceLoader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class RuntimeContextTest {

    @Test
    public void create() {

        RuntimeContext expected = mock(RuntimeContext.class);

        RuntimeContext result;
        try(var mockedStaticServiceLoader = mockStatic(ServiceLoader.class)) {

            ServiceLoader<RuntimeContext> serviceLoader = mock(ServiceLoader.class);
            when(serviceLoader.findFirst()).thenReturn(Optional.of(expected));

            mockedStaticServiceLoader.when(() -> ServiceLoader.load(RuntimeContext.class)).thenReturn(serviceLoader);

            result = RuntimeContext.getInstance();

            verify(serviceLoader).findFirst();
            verifyNoMoreInteractions(serviceLoader);
            mockedStaticServiceLoader.verify(() -> ServiceLoader.load(RuntimeContext.class));
            mockedStaticServiceLoader.verifyNoMoreInteractions();

        }

        assertThat(result).isSameAs(expected);

    }


}
