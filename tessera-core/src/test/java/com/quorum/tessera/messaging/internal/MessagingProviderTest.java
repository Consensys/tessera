package com.quorum.tessera.messaging.internal;

import com.quorum.tessera.messaging.Messaging;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MessagingProviderTest {

  private MessagingHolder holder;
  private Messaging messaging;


  @Before
  public void setUp()  {
    holder = mock(MessagingHolder.class);
  }

  @Test
  public void testMessagingProvider() {
    messaging = mock(Messaging.class);
    when(holder.getMessaging()).thenReturn(Optional.ofNullable(messaging));
    assertEquals(true,holder.getMessaging().isPresent());
  }
}
