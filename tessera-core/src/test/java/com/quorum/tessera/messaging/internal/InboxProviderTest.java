package com.quorum.tessera.messaging.internal;

import com.quorum.tessera.data.EncryptedMessageDAO;
import com.quorum.tessera.messaging.Inbox;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class InboxProviderTest {
  InboxHolder inboxHolder;
  Inbox inbox;
  EncryptedMessageDAO encryptedMessageDAO;
  InboxImpl inboxImpl;

  @Before
  public void setUp() {
    inboxHolder = mock(InboxHolder.class);
    inbox = mock(Inbox.class);
    encryptedMessageDAO = mock(EncryptedMessageDAO.class);
    inboxImpl = new InboxImpl(encryptedMessageDAO);
  }

  @Test
  public void testInboxProvider() {
    when(inboxHolder.getInbox()).thenReturn(Optional.ofNullable(inbox));
    assertEquals(Optional.of(inbox),inboxHolder.getInbox());
    doReturn(inbox).when(inboxHolder).store(inboxImpl);
  }
}
