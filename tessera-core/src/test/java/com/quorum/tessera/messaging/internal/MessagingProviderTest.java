package com.quorum.tessera.messaging.internal;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.messaging.Courier;
import com.quorum.tessera.messaging.Inbox;
import com.quorum.tessera.messaging.Messaging;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mockStatic;

public class MessagingProviderTest {

  @Test
  public void clearHolder() {
    MessagingHolder.INSTANCE.store(null);
    assertThat(MessagingHolder.INSTANCE.getMessaging()).isNotPresent();
  }


  @Test
  public void provider() {

    try (var mockedStaticConfigFactory = mockStatic(ConfigFactory.class);
         var mockedStaticEnclave = mockStatic(Enclave.class);
         var mockedStaticCourier = mockStatic(Courier.class);
         var mockedStaticInbox = mockStatic(Inbox.class)) {

      ConfigFactory configFactory = mock(ConfigFactory.class);
      Config config = mock(Config.class);
      when(configFactory.getConfig()).thenReturn(config);
      mockedStaticConfigFactory.when(ConfigFactory::create).thenReturn(configFactory);

      mockedStaticEnclave.when(Enclave::create).thenReturn(mock(Enclave.class));
      mockedStaticCourier.when(Courier::create).thenReturn(mock(Courier.class));
      mockedStaticInbox.when(Inbox::create).thenReturn(mock(Inbox.class));

      Messaging messaging = MessagingProvider.provider();
      assertThat(messaging).isNotNull();

      assertThat(MessagingProvider.provider())
        .describedAs("Second invocation should return same instance")
        .isSameAs(messaging);

      mockedStaticEnclave.verify(Enclave::create);
      mockedStaticEnclave.verifyNoMoreInteractions();
      mockedStaticCourier.verify(Courier::create);
      mockedStaticCourier.verifyNoMoreInteractions();
      mockedStaticInbox.verify(Inbox::create);
      mockedStaticInbox.verifyNoMoreInteractions();

    }
  }

  @Test
  public void defaultConstructorForCoverage() {
    assertThat(new MessagingProvider()).isNotNull();
  }
}
