package com.quorum.tessera.enclave;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.encryption.PublicKey;
import org.junit.Test;

public class RawTransactionBuilderTest {

  @Test
  public void build() {

    final byte[] encryptedKey = "KEY".getBytes();

    final byte[] encryptedPayload = "PAYLOAD".getBytes();

    final byte[] nonce = "NONCE".getBytes();

    final PublicKey from = PublicKey.from("SOMEKEY".getBytes());

    final RawTransaction txn =
        RawTransactionBuilder.create()
            .withEncryptedKey(encryptedKey)
            .withEncryptedPayload(encryptedPayload)
            .withNonce(nonce)
            .withFrom(from)
            .build();

    assertThat(txn).isNotNull();
    assertThat(txn.getEncryptedKey()).isEqualTo(encryptedKey);
    assertThat(txn.getEncryptedPayload()).isEqualTo(encryptedPayload);
    assertThat(txn.getFrom()).isEqualTo(from);
    assertThat(txn.getNonce().getNonceBytes()).isEqualTo(nonce);
  }
}
