package com.quorum.tessera.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class MessageHashTest {

  @Test
  public void messageHashMakesCopyOfInput() {

    final byte[] testMessage = "test_message".getBytes();

    final MessageHash hash = new MessageHash(testMessage);

    assertThat(hash).isNotEqualTo(testMessage);
    Assertions.assertThat(Arrays.equals(testMessage, hash.getHashBytes())).isTrue();
  }

  @Test
  public void differentInstancesOfSameBytesIsEqual() {

    final byte[] testMessage = "test_message".getBytes();

    final MessageHash hash1 = new MessageHash(testMessage);
    final MessageHash hash2 = new MessageHash(testMessage);

    assertThat(hash1).isEqualTo(hash2);
  }

  @Test
  public void differentObjectTypesAreNotEqual() {

    final byte[] testMessage = "test_message".getBytes();

    final MessageHash hash1 = new MessageHash(testMessage);

    assertThat(hash1).isNotEqualTo("test_message");
  }

  @Test
  public void sameObjectIsEqual() {
    final MessageHash hash = new MessageHash("I LOVE SPARROWS".getBytes());
    assertThat(hash).isEqualTo(hash).hasSameHashCodeAs(hash);
  }

  @Test
  public void toStringOutputsCorrectString() {

    // dmFs is "val" encoded as base64 in UTF_8
    final MessageHash hash = new MessageHash("val".getBytes());

    final String toString = hash.toString();

    assertThat(toString).isEqualTo("dmFs");
  }

  @Test
  public void sameObjectIsEqualAccessors() {
    final MessageHash hash = new MessageHash();
    hash.setHashBytes("I LOVE SPARROWS".getBytes());
    assertThat(hash).isEqualTo(hash).hasSameHashCodeAs(hash);
  }
}
