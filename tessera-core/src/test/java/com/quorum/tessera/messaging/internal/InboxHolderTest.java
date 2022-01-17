package com.quorum.tessera.messaging.internal;

import com.quorum.tessera.messaging.Inbox;
import com.quorum.tessera.messaging.Messaging;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InboxHolderTest extends TestCase {

  Inbox inbox;
  InboxHolder inboxHolder;

  @Before
  protected void setUp() throws Exception {
    inbox = mock(Inbox.class);
    inboxHolder = mock(InboxHolder.class);
  }

  @Test
  public void testGetInbox() {
    Optional<Inbox> optionalMessaging =  Optional.ofNullable(inbox);
    when(inboxHolder.getInbox()).thenReturn(optionalMessaging);
  }

  @Test
  public void testStore() {
    when(inboxHolder.store(any())).thenReturn(inbox);
  }
}
