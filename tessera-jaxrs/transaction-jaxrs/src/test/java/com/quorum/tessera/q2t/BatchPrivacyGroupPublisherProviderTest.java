package com.quorum.tessera.q2t;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.privacygroup.publish.BatchPrivacyGroupPublisher;
import com.quorum.tessera.privacygroup.publish.PrivacyGroupPublisher;
import com.quorum.tessera.q2t.internal.BatchPrivacyGroupPublisherProvider;
import org.junit.Test;

public class BatchPrivacyGroupPublisherProviderTest {

  @Test
  public void provider() {

    PrivacyGroupPublisher privacyGroupPublisher = mock(PrivacyGroupPublisher.class);

    BatchPrivacyGroupPublisher result;
    try (var privacyGroupPublisherMockedStatic = mockStatic(PrivacyGroupPublisher.class)) {

      privacyGroupPublisherMockedStatic
          .when(PrivacyGroupPublisher::create)
          .thenReturn(privacyGroupPublisher);

      result = BatchPrivacyGroupPublisherProvider.provider();

      privacyGroupPublisherMockedStatic.verify(PrivacyGroupPublisher::create);
      privacyGroupPublisherMockedStatic.verifyNoMoreInteractions();
    }

    assertThat(result).isNotNull();

    verifyNoInteractions(privacyGroupPublisher);
  }

  @Test
  public void defaultConstructorForCoverage() {
    assertThat(new BatchPrivacyGroupPublisherProvider()).isNotNull();
  }
}
