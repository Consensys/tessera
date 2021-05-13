package com.quorum.tessera.privacygroup.publisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.privacygroup.publish.BatchPrivacyGroupPublisher;
import java.util.Optional;
import java.util.ServiceLoader;
import org.junit.Test;

public class BatchPrivacyGroupPublisherTest {
  @Test
  public void create() {
    BatchPrivacyGroupPublisher privacyGroupPublisher = mock(BatchPrivacyGroupPublisher.class);
    ServiceLoader<BatchPrivacyGroupPublisher> serviceLoader = mock(ServiceLoader.class);
    when(serviceLoader.findFirst()).thenReturn(Optional.of(privacyGroupPublisher));
    BatchPrivacyGroupPublisher result;
    try (var serviceLoaderMockedStatic = mockStatic(ServiceLoader.class)) {

      serviceLoaderMockedStatic
          .when(() -> ServiceLoader.load(BatchPrivacyGroupPublisher.class))
          .thenReturn(serviceLoader);

      result = BatchPrivacyGroupPublisher.create();

      serviceLoaderMockedStatic.verify(() -> ServiceLoader.load(BatchPrivacyGroupPublisher.class));
      serviceLoaderMockedStatic.verifyNoMoreInteractions();
    }
    verify(serviceLoader).findFirst();
    verifyNoMoreInteractions(serviceLoader);
    assertThat(result).isNotNull().isSameAs(privacyGroupPublisher);
  }
}
