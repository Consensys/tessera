package com.quorum.tessera.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

public class MessageIdTest {
  MessageId messageId;

  @Before
  public void setUp() {
    messageId = mock(MessageId.class);
  }

  @Test
  public void testParseMessageId() {
    assertThat(MessageId.parseMessageId("string")).isInstanceOf(MessageId.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWithNullParameter() {
    MessageId.parseMessageId(null);
  }

  @Test
  public void testHashCodeAndEquals() {
    MessageId messageId1 = new MessageId("ok".getBytes());
    MessageId messageId2 = new MessageId("ok".getBytes());
    assertThat(messageId1.hashCode()).isNotSameAs(messageId2);
    assertThat(messageId1.equals(messageId2));
  }

  @Test
  public void testToString() {
    MessageId messageId1 = new MessageId("ok".getBytes());
    assertThat(messageId1.toString()).isNotEmpty();
  }

  @Test
  public void testGetValue() {
    MessageId messageId1 = new MessageId("ok".getBytes());
    assertThat(messageId1.getValue()).isNotNull();
    assertThat(messageId1.getValue().length > 0);
    byte[] value = new byte[256];
    when(messageId.getValue()).thenReturn(value);
  }
}
