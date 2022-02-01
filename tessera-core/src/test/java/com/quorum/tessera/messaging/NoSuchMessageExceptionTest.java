package com.quorum.tessera.messaging;

import static org.mockito.Mockito.mock;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class NoSuchMessageExceptionTest {

  private MessageId messageId;
  private NoSuchMessageException noSuchMessageException;

  @Before
  public void setUp() {
    messageId = mock(MessageId.class);
    noSuchMessageException = mock(NoSuchMessageException.class);
  }

  @Test
  public void testNoSuchMessageException() {
    String testData = "this is test";
    MessageId messageId = new MessageId(testData.getBytes());
    NoSuchMessageException noSuchMessageException = new NoSuchMessageException(messageId);
    Assert.assertNotNull(noSuchMessageException.getMessageId());
    Assert.assertNotNull(noSuchMessageException);
  }
}
