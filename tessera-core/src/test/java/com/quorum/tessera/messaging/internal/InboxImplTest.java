package com.quorum.tessera.messaging.internal;

import static org.mockito.Mockito.*;

import com.quorum.tessera.data.EncryptedMessageDAO;
import com.quorum.tessera.data.MessageHash;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

public class InboxImplTest extends TestCase {
  private InboxImpl inbox;
  private EncryptedMessageDAO dao;

  @Before
  public void setUp() throws Exception {
    dao = mock(EncryptedMessageDAO.class);
    inbox = new InboxImpl(dao);
  }

  @Test
  public void testHash() {
    String testData = "This is the test data for for Checking SHA-256 and Truncation";
    byte[] dataForHashConversion = testData.getBytes();
    MessageHash messageHash = inbox.hash(dataForHashConversion);
    assertEquals(messageHash.getHashBytes().length, 20);
  }

  /*@Test
  public void testStream() {
    List<MessageHash> mockList = new ArrayList<>();
    MessageHash messageHash = mock(MessageHash.class);
    mockList.add(messageHash);
    when(dao.retrieveMessageHashes(any(), any())).thenReturn(mockList);
  }*/

  @Test
  public void testDelete() {
    MessageHash messageHash = mock(MessageHash.class);
    doNothing().when(dao).delete(messageHash);
  }


  /*public void testDeleteForException() {
    MessageHash messageHash = null;
    doThrow(EntityNotFoundException.class).when(dao).delete(eq(messageHash));
  }*/
}
