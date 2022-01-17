package com.quorum.tessera.messaging.internal;

import com.quorum.tessera.messaging.Messaging;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.mockito.Mockito.*;

public class MessagingHolderTest extends TestCase {
  Messaging messaging;
  MessagingHolder messagingHolder;

  @Before
  protected void setUp() throws Exception {
    messaging = mock(Messaging.class);
    messagingHolder = mock(MessagingHolder.class);
  }

  @Test
  public void testGetMessaging() {
    Optional<Messaging> optionalMessaging =  Optional.ofNullable(messaging);
    when(messagingHolder.getMessaging()).thenReturn(optionalMessaging);
  }

  @Test
  public void testStore() {
    when(messagingHolder.store(any())).thenReturn(messaging);
  }
}
