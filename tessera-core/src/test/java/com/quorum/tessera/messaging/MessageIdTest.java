package com.quorum.tessera.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import org.junit.Test;

public class MessageIdTest {

  @Test
  public void testParseMessageId() {
    assertThat(MessageId.parseMessageId("string")).isInstanceOf(MessageId.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWithNullParameter() {
    assertThat(MessageId.parseMessageId(null)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void testHashCodeAndEquals() {
    final MessageId messageId1 = new MessageId("ok".getBytes());
    final MessageId messageId2 = new MessageId("ok".getBytes());
    assertThat(messageId1).isInstanceOf(MessageId.class);
    assertThat(messageId2).isInstanceOf(MessageId.class);
    assertThat(messageId1.hashCode()).isNotSameAs(messageId2);
    assertThat(messageId1.equals(messageId2)).isTrue();
    assertThat(Arrays.equals(messageId1.getValue(), messageId2.getValue()));

    assertEquals(true, new MessageId("hello".getBytes()).equals(new MessageId("hello".getBytes())));
    assertEquals(false, new MessageId("lo".getBytes()).equals(new MessageId("hello".getBytes())));
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
  }
}
