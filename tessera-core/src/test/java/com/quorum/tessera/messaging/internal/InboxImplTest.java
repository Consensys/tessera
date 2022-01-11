package com.quorum.tessera.messaging.internal;

import static org.mockito.Mockito.mock;

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
}
