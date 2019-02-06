package com.quorum.tessera.enclave;

import com.quorum.tessera.encryption.PublicKey;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class RawTransactionBuilderTest {

    @Test
    public void build() {

        byte[] encryptedKey = "KEY".getBytes();

        byte[] encryptedPayload = "PAYLOAD".getBytes();

        byte[] nonce = "NONCE".getBytes();

        PublicKey from = PublicKey.from("SOMEKEY".getBytes());

        RawTransaction txn = RawTransactionBuilder.create()
                .withEncryptedKey(encryptedKey)
                .withEncryptedPayload(encryptedPayload)
                .withNonce(nonce)
                .withFrom(from)
                .build();

        assertThat(txn).isNotNull();

        assertThat(txn.getEncryptedKey()).isEqualTo(encryptedKey);
        assertThat(txn.getEncryptedPayload()).isEqualTo(encryptedPayload);
        assertThat(txn.getFrom()).isSameAs(from);
        assertThat(txn.getNonce().getNonceBytes()).isEqualTo(nonce);

    }

}
