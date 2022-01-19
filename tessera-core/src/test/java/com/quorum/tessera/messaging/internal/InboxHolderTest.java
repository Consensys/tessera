package com.quorum.tessera.messaging.internal;

import com.quorum.tessera.data.EncryptedMessageDAO;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.messaging.Courier;
import com.quorum.tessera.messaging.Inbox;
import com.quorum.tessera.messaging.Messaging;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InboxHolderTest {

  Inbox inbox;
  InboxHolder inboxHolder;
  InboxImpl inboxImpl;
  EncryptedMessageDAO dao;

  @Before
  public void setUp()  {
    inbox = mock(Inbox.class);
    inboxHolder = mock(InboxHolder.class);
    dao = mock(EncryptedMessageDAO.class);
  }

  @Test
  public void testGetInbox() {
    Optional<Inbox> optionalInbox =  Optional.ofNullable(inbox);
    when(inboxHolder.getInbox()).thenReturn(optionalInbox);
    assertThat(InboxHolder.INSTANCE).isNotNull();
    when(inboxHolder.getInbox()).thenReturn(optionalInbox);
    InboxHolder holder =  InboxHolder.INSTANCE.INSTANCE;
    assertThat(inboxHolder.getInbox()).isNotNull();
    assertThat(holder.getInbox()).isNotNull();

  }

  @Test
  public void testStore() {
    inboxImpl = new InboxImpl(dao);
    when(inboxHolder.store(any())).thenReturn(inbox);
    InboxHolder holder =  InboxHolder.INSTANCE;
    assertThat(inboxHolder.getInbox()).isNotNull();
    assertThat(holder.getInbox()).isNotNull();
    assertThat(inboxImpl).isNotNull();
    Assert.assertNotNull(holder.store(inboxImpl));
  }
}
