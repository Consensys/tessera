package com.quorum.tessera.enclave;

import com.quorum.tessera.encryption.PublicKey;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class EncodedPayloadBuilderTest {

    @Test
    public void build() {

        final PublicKey senderKey = PublicKey.from("SENDER_KEY".getBytes());

        final PublicKey recipientKey = PublicKey.from("RECIPIENT_KEY".getBytes());

        final byte[] cipherText = "cipherText".getBytes();
        final byte[] cipherTextNonce = "cipherTextNonce".getBytes();

        final byte[] recipientNonce = "recipientNonce".getBytes();
        final byte[] recipientBox = "recipientBox".getBytes();

        final EncodedPayload sample =
                EncodedPayloadBuilder.create()
                        .withSenderKey(senderKey)
                        .withCipherText(cipherText)
                        .withCipherTextNonce(cipherTextNonce)
                        .withRecipientBoxes(Arrays.asList(recipientBox))
                        .withRecipientNonce(recipientNonce)
                        .withRecipientKeys(recipientKey)
                        .build();

        assertThat(sample.getSenderKey()).isEqualTo(senderKey);
        assertThat(sample.getCipherText()).isEqualTo("cipherText".getBytes());
        assertThat(sample.getCipherTextNonce().getNonceBytes()).isEqualTo(cipherTextNonce);
        assertThat(sample.getRecipientNonce().getNonceBytes()).isEqualTo(recipientNonce);
        assertThat(sample.getRecipientBoxes()).hasSize(1).containsExactlyInAnyOrder(recipientBox);
        assertThat(sample.getRecipientKeys()).hasSize(1).containsExactlyInAnyOrder(recipientKey);
    }
}
