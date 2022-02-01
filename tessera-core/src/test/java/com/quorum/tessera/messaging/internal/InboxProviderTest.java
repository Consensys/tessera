package com.quorum.tessera.messaging.internal;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.data.EncryptedMessageDAO;
import com.quorum.tessera.messaging.Inbox;
import org.junit.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class InboxProviderTest {

  @Test
  public void clearHolder() {
    InboxHolder.INSTANCE.store(null);
    assertThat(InboxHolder.INSTANCE.getInbox()).isNotPresent();
  }

  @Test
  public void provider() {

    try (var mockedStaticConfigFactory = mockStatic(ConfigFactory.class);
         var mockedStaticEncryptedMessageDAO = mockStatic(EncryptedMessageDAO.class))
         {

      ConfigFactory configFactory = mock(ConfigFactory.class);
      Config config = mock(Config.class);
      when(configFactory.getConfig()).thenReturn(config);
      mockedStaticConfigFactory.when(ConfigFactory::create).thenReturn(configFactory);

      mockedStaticEncryptedMessageDAO
        .when(EncryptedMessageDAO::create)
        .thenReturn(mock(EncryptedMessageDAO.class));

           Inbox inbox = InboxProvider.provider();
           assertThat(inbox).isNotNull();

           assertThat(InboxProvider.provider())
             .describedAs("Second invocation should return same instance")
             .isSameAs(inbox);

           mockedStaticEncryptedMessageDAO.verify(EncryptedMessageDAO::create);
           mockedStaticEncryptedMessageDAO.verifyNoMoreInteractions();
    }
  }

  @Test
  public void defaultConstructorForCoverage() {
    assertThat(new InboxProvider()).isNotNull();
  }

}



