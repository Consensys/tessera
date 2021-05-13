package com.quorum.tessera.privacygroup.publish;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.quorum.tessera.config.Config;
import org.junit.Test;

public class PrivacyGroupPublisherFactoryTest {

  @Test
  public void newFactory() {

    PrivacyGroupPublisherFactory factory =
        PrivacyGroupPublisherFactory.newFactory(mock(Config.class));
    assertThat(factory).isNotNull();
  }
}
