package com.quorum.tessera.messaging;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageId {

  private static final Logger LOGGER = LoggerFactory.getLogger(MessageId.class);

  private byte[] value;

  public static MessageId parseMessageId(String stringValue) {

    final int length = (stringValue == null) ? 0 : stringValue.length();
    if (length == 0 || ((length % 2) != 0)) {
      throw new IllegalArgumentException(
          "String representation of a message ID cannot be empty, null, or uneven in length");
    }

    // Iterate the pairs of characters in the string
    try {
      int index = 0;
      byte[] buffer = new byte[length / 2];

      for (CharacterIterator it = new StringCharacterIterator(stringValue);
          it.current() != CharacterIterator.DONE;
          it.next(), ++index) {
        final int radix = 16;
        final int left = Character.digit(it.current(), radix);
        final int right = Character.digit(it.next(), radix);
        buffer[index] = (byte) ((left << 4) + right);
      }
      LOGGER.info("Parsed {} as {}", stringValue, buffer);
      return new MessageId(buffer);
    } catch (Exception ex) {
      throw new IllegalArgumentException(ex);
    }
  }

  public MessageId(byte[] value) {
    this.value = value;
  }

  public byte[] getValue() {
    return this.value;
  }

  @Override
  public boolean equals(final Object o) {
    return (o instanceof MessageId) && Arrays.equals(getValue(), ((MessageId) o).getValue());
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(getValue());
  }

  @Override
  public String toString() {

    StringBuilder builder = new StringBuilder();
    for (byte b : value) {
      builder.append(String.format("%02x", b));
    }
    return builder.toString();
  }
}
