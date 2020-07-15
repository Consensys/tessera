package com.quorum.tessera.enclave;

import com.quorum.tessera.encryption.PublicKey;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class EncodedPayloadBuilderTest {

    final PublicKey senderKey = PublicKey.from("SENDER_KEY".getBytes());

    final PublicKey recipientKey = PublicKey.from("RECIPIENT_KEY".getBytes());

    final byte[] cipherText = "cipherText".getBytes();

    final byte[] cipherTextNonce = "cipherTextNonce".getBytes();

    final byte[] recipientNonce = "recipientNonce".getBytes();

    final byte[] recipientBox = "recipientBox".getBytes();

    @Test
    public void build() {

        final EncodedPayload sample =
                EncodedPayload.Builder.create()
                        .withSenderKey(senderKey)
                        .withCipherText(cipherText)
                        .withCipherTextNonce(cipherTextNonce)
                        .withRecipientBoxes(Arrays.asList(recipientBox))
                        .withRecipientNonce(recipientNonce)
                        .withRecipientKeys(Arrays.asList(recipientKey))
                        .build();

        assertThat(sample.getSenderKey()).isEqualTo(senderKey);
        assertThat(sample.getCipherText()).isEqualTo("cipherText".getBytes());
        assertThat(sample.getCipherTextNonce().getNonceBytes()).isEqualTo(cipherTextNonce);
        assertThat(sample.getRecipientNonce().getNonceBytes()).isEqualTo(recipientNonce);
        assertThat(sample.getRecipientBoxes()).hasSize(1).containsExactlyInAnyOrder(RecipientBox.from(recipientBox));
        assertThat(sample.getRecipientKeys()).hasSize(1).containsExactlyInAnyOrder(recipientKey);
    }

    @Test
    public void from() {
        final EncodedPayload sample =
            EncodedPayload.Builder.create()
                .withSenderKey(senderKey)
                .withCipherText(cipherText)
                .withCipherTextNonce(cipherTextNonce)
                .withRecipientBoxes(Arrays.asList(recipientBox))
                .withRecipientNonce(recipientNonce)
                .withRecipientKeys(Arrays.asList(recipientKey))
                .build();

        EncodedPayload result = EncodedPayload.Builder.from(sample).build();

        assertThat(result)
            .isNotSameAs(sample)
            .isEqualTo(sample);

        EqualsVerifier.forClass(EncodedPayload.class)
            .usingGetClass()
            .verify();

    }
}
