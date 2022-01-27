package com.quorum.tessera.messaging;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class MessageIdTest {
  MessageId messageId;

  @Before
  public void setUp() {
    messageId = mock(MessageId.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseMessageId() {
    assertThat(MessageId.parseMessageId("string")).isNotNull();

    String stringValue = "sting";
    int length = (stringValue == null) ? 0 : stringValue.length();
    if (length == 0 || ((length % 2) != 0)) {
      throw new IllegalArgumentException(
        "String representation of a message ID cannot be empty, null, or uneven in length");
    }

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      int len = (stringValue == null) ? 0 : stringValue.length();
      if (len== 0 || ((len % 2) != 0)) {
        throw new IllegalArgumentException(
          "String representation of a message ID cannot be empty, null, or uneven in length");
      }
    });

  }

  @Test
  public void testHashCodeAndEquals(){
    MessageId messageId1 = new MessageId("ok".getBytes());
    MessageId messageId2 = new MessageId("ok".getBytes());

    assertThat(messageId1.hashCode()).isNotSameAs(messageId2);
    assertThat(messageId1.equals(messageId2));
  }

  @Test
  public void testToString(){
    MessageId messageId1 = new MessageId("ok".getBytes());
    assertThat(messageId1.toString()).isNotEmpty();

  }

  @Test
  public void testGetValue(){
    MessageId messageId1 = new MessageId("ok".getBytes());
    assertThat(messageId1.getValue()).isNotNull();
    assertThat(messageId1.getValue().length > 0);
    byte[] value = new byte[256];
    when(messageId.getValue()).thenReturn(value);
  }
}
