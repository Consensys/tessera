package com.quorum.tessera.recovery.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.ServiceLoader;
import org.junit.Test;

public class BatchWorkflowFactoryTest {
  @Test
  public void create() {
    BatchWorkflowFactory expected = mock(BatchWorkflowFactory.class);
    BatchWorkflowFactory result;
    try (var staticServiceLoader = mockStatic(ServiceLoader.class)) {
      ServiceLoader<BatchWorkflowFactory> serviceLoader = mock(ServiceLoader.class);
      when(serviceLoader.findFirst()).thenReturn(Optional.of(expected));
      staticServiceLoader
          .when(() -> ServiceLoader.load(BatchWorkflowFactory.class))
          .thenReturn(serviceLoader);

      result = BatchWorkflowFactory.create();

      staticServiceLoader.verify(() -> ServiceLoader.load(BatchWorkflowFactory.class));
      staticServiceLoader.verifyNoMoreInteractions();

      verify(serviceLoader).findFirst();
      verifyNoMoreInteractions(serviceLoader);
    }

    assertThat(result).isSameAs(expected);
  }
}
