package com.quorum.tessera.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.quorum.tessera.enclave.RawTransaction;
import java.util.Objects;
import org.junit.Test;

public class EncryptedRawTransactionTest {

  @Test
  public void createInstance() {

    byte[] payload = "PAYLOAD".getBytes();
    byte[] key = "key".getBytes();
    byte[] nonce = "nonce".getBytes();
    byte[] from = "from".getBytes();

    EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction();
    MessageHash hash = mock(MessageHash.class);
    encryptedRawTransaction.setHash(hash);
    encryptedRawTransaction.setEncryptedPayload(payload);
    encryptedRawTransaction.setEncryptedKey(key);
    encryptedRawTransaction.setNonce(nonce);
    encryptedRawTransaction.setSender(from);

    assertThat(encryptedRawTransaction.getHash()).isSameAs(hash);
    assertThat(encryptedRawTransaction.getEncryptedPayload()).isSameAs(payload);
    assertThat(encryptedRawTransaction.getEncryptedKey()).isSameAs(key);
    assertThat(encryptedRawTransaction.getNonce()).isSameAs(nonce);
    assertThat(encryptedRawTransaction.getSender()).isSameAs(from);
  }

  @Test
  public void createInstanceWithConstructorArgs() {

    byte[] payload = "PAYLOAD".getBytes();
    MessageHash hash = mock(MessageHash.class);
    byte[] key = "key".getBytes();
    byte[] nonce = "nonce".getBytes();
    byte[] from = "from".getBytes();
    EncryptedRawTransaction encryptedRawTransaction =
        new EncryptedRawTransaction(hash, payload, key, nonce, from);

    assertThat(encryptedRawTransaction.getHash()).isSameAs(hash);
    assertThat(encryptedRawTransaction.getEncryptedPayload()).isSameAs(payload);
    assertThat(encryptedRawTransaction.getEncryptedKey()).isSameAs(key);
    assertThat(encryptedRawTransaction.getNonce()).isSameAs(nonce);
    assertThat(encryptedRawTransaction.getSender()).isSameAs(from);
  }

  @Test
  public void subclassesEqual() {

    class OtherClass extends EncryptedRawTransaction {}

    final OtherClass other = new OtherClass();
    final EncryptedRawTransaction et = new EncryptedRawTransaction();

    final boolean equal = Objects.equals(et, other);

    assertThat(equal).isTrue();
  }

  @Test
  public void differentClassesNotEqual() {

    final Object other = "OTHER";
    final EncryptedRawTransaction et = new EncryptedRawTransaction();

    final boolean equal = Objects.equals(et, other);

    assertThat(equal).isFalse();
  }

  @Test
  public void sameObjectHasSameHash() {

    final EncryptedRawTransaction et = new EncryptedRawTransaction();

    assertThat(et.hashCode()).isEqualTo(et.hashCode());
  }

  @Test
  public void toRawTransaction() {
    byte[] payload = "PAYLOAD".getBytes();
    MessageHash hash = mock(MessageHash.class);
    byte[] key = "key".getBytes();
    byte[] nonce = "nonce".getBytes();
    byte[] from = "from".getBytes();

    EncryptedRawTransaction encryptedRawTransaction =
        new EncryptedRawTransaction(hash, payload, key, nonce, from);

    RawTransaction rawTransaction = encryptedRawTransaction.toRawTransaction();

    assertThat(rawTransaction).isNotNull();
    assertThat(rawTransaction.getEncryptedPayload()).isEqualTo(payload);
    assertThat(rawTransaction.getEncryptedKey()).isEqualTo(key);
    assertThat(rawTransaction.getNonce().getNonceBytes()).isEqualTo(nonce);
    assertThat(rawTransaction.getFrom().getKeyBytes()).isEqualTo(from);
  }
}
