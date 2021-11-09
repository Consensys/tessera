package com.quorum.tessera.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.quorum.tessera.enclave.EncodedPayload;
import java.util.Objects;
import org.junit.Test;

public class EncryptedTransactionTest {

  @Test
  public void createInstance() {

    byte[] payload = "PAYLOAD".getBytes();

    EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
    MessageHash hash = mock(MessageHash.class);
    encryptedTransaction.setHash(hash);
    encryptedTransaction.setEncodedPayload(payload);

    assertThat(encryptedTransaction.getHash()).isSameAs(hash);
    assertThat(encryptedTransaction.getEncodedPayload()).isSameAs(payload);
  }

  @Test
  public void createInstanceWithConstructorArgs() {

    EncodedPayload payload = mock(EncodedPayload.class);
    MessageHash hash = mock(MessageHash.class);
    EncryptedTransaction encryptedTransaction = new EncryptedTransaction(hash, payload);

    assertThat(encryptedTransaction.getHash()).isSameAs(hash);
    assertThat(encryptedTransaction.getPayload()).isSameAs(payload);
  }

  @Test
  public void subclassesEqual() {

    class OtherClass extends EncryptedTransaction {}

    final OtherClass other = new OtherClass();
    final EncryptedTransaction et = new EncryptedTransaction();

    final boolean equal = Objects.equals(et, other);

    assertThat(equal).isTrue();
  }

  @Test
  public void differentClassesNotEqual() {

    final Object other = "OTHER";
    final EncryptedTransaction et = new EncryptedTransaction();

    final boolean equal = Objects.equals(et, other);

    assertThat(equal).isFalse();
  }

  @Test
  public void sameObjectHasSameHash() {

    final EncryptedTransaction et = new EncryptedTransaction();

    assertThat(et.hashCode()).isEqualTo(et.hashCode());
  }
}
