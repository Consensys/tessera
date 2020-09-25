package com.quorum.tessera.threading;

import org.junit.Test;

import java.util.concurrent.CompletionService;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class CompletionServiceFactoryTest {

    @Test
    public void create() {
        CompletionServiceFactory<Void> factory = new CompletionServiceFactory<>();
        Executor executor = mock(Executor.class);

        CompletionService<Void> service = factory.create(executor);
        assertThat(service).isNotNull();
    }
}
