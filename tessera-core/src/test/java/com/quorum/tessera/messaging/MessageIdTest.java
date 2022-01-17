package com.quorum.tessera.messaging;

import junit.framework.TestCase;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class MessageIdTest extends TestCase {
  MessageId messageId;

  @Override
  protected void setUp() throws Exception {
    messageId = mock(MessageId.class);
  }

  @Test
  public void testParseMessageId() {
    when(null==messageId.toString()).thenThrow(new IllegalArgumentException(
      "String representation of a message ID cannot be empty, null, or uneven in length"));

  }
}
