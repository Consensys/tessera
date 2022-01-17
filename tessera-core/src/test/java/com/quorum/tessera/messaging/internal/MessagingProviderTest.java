package com.quorum.tessera.messaging.internal;

import com.quorum.tessera.messaging.Messaging;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MessagingProviderTest extends TestCase {

  private MessagingHolder holder;
  private Messaging messaging;

  @Override
  @Before
  public void setUp() throws Exception {
    holder = mock(MessagingHolder.class);
  }

  @Test
  public void testMessagingProvider() {
    messaging = mock(Messaging.class);
    when(holder.getMessaging()).thenReturn(Optional.ofNullable(messaging));
    assertEquals(true,holder.getMessaging().isPresent());
  }
}
