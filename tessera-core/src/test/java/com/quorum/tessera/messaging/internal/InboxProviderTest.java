package com.quorum.tessera.messaging.internal;

import com.quorum.tessera.data.EncryptedMessageDAO;
import com.quorum.tessera.messaging.Inbox;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.mockito.Mockito.*;

public class InboxProviderTest extends TestCase {
  InboxHolder inboxHolder;
  Inbox inbox;
  EncryptedMessageDAO encryptedMessageDAO;
  InboxImpl inboxImpl;

  @Before
  protected void setUp() throws Exception {
    inboxHolder = mock(InboxHolder.class);
    inbox = mock(Inbox.class);
    encryptedMessageDAO = mock(EncryptedMessageDAO.class);
    //inboxImpl = new InboxImpl(encryptedMessageDAO);

  }


  @Test
  public void testInboxProvider() {
    when(inboxHolder.getInbox()).thenReturn(Optional.ofNullable(inbox));
    assertEquals(true, inboxHolder.getInbox().isPresent());
    //doReturn(encryptedMessageDAO).when(EncryptedMessageDAO.create());
    //when(EncryptedMessageDAO.create()).thenReturn(encryptedMessageDAO);
    //when(inboxHolder.store(inbox)).thenReturn(inboxImpl);
  }
}
