package com.quorum.tessera.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.*;
import org.junit.Test;

public class EncryptedMessageTest {

  @Test
  public void createInstance() {

    EncryptedMessage encryptedMessage = new EncryptedMessage();
    MessageHash hash = mock(MessageHash.class);
    encryptedMessage.setHash(hash);
    encryptedMessage.setTimestamp(System.currentTimeMillis());
    encryptedMessage.setContent("test".getBytes());
    encryptedMessage.onPersist();

    assertThat(encryptedMessage.getHash()).isSameAs(hash);
    assertThat(encryptedMessage.getContent()).isNotNull();
    assertThat(encryptedMessage.getTimestamp()).isNotNull();
  }

  @Test
  public void createInstanceWithConstructorArgs() {

    MessageHash hash = mock(MessageHash.class);
    EncryptedMessage encryptedMessage = new EncryptedMessage(hash, "test".getBytes());

    assertThat(encryptedMessage.getHash()).isSameAs(hash);
  }

  @Test
  public void subclassesEqual() {

    class OtherClass extends EncryptedMessage {}

    final OtherClass other = new OtherClass();
    final EncryptedMessage et = new EncryptedMessage();

    final boolean equal = Objects.equals(et, other);

    assertThat(equal).isTrue();
  }

  @Test
  public void differentClassesNotEqual() {

    final Object other = "OTHER";
    final EncryptedMessage et = new EncryptedMessage();

    final boolean equal = Objects.equals(et, other);

    assertThat(equal).isFalse();
  }

  @Test
  public void sameObjectHasSameHash() {

    final EncryptedMessage et = new EncryptedMessage();

    assertThat(et.hashCode()).isEqualTo(et.hashCode());
  }
}
