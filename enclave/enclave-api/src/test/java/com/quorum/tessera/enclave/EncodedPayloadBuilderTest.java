package com.quorum.tessera.enclave;

import com.quorum.tessera.encryption.PublicKey;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class EncodedPayloadBuilderTest {

    final PublicKey senderKey = PublicKey.from("SENDER_KEY".getBytes());

    final PublicKey recipientKey = PublicKey.from("RECIPIENT_KEY".getBytes());

    final byte[] cipherText = "cipherText".getBytes();

    final byte[] cipherTextNonce = "cipherTextNonce".getBytes();

    final byte[] recipientNonce = "recipientNonce".getBytes();

    final byte[] recipientBox = "recipientBox".getBytes();

    final Map<TxHash, byte[]> affectedContractTransactionsRaw =
        new HashMap<>() {
            {
                put(
                    new TxHash(
                        "bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSw2J5hQ=="),
                    "transaction".getBytes());
            }
        };

    final byte[] execHash = "execHash".getBytes();
    @Test
    public void build() {

        final EncodedPayload sample =
                EncodedPayload.Builder.create()
                        .withSenderKey(senderKey)
                        .withCipherText(cipherText)
                        .withCipherTextNonce(cipherTextNonce)
                        .withRecipientBox(recipientBox)
                        .withRecipientNonce(recipientNonce)
                        .withPrivacyFlag(3)
                        .withAffectedContractTransactions(affectedContractTransactionsRaw)
                        .withExecHash(execHash)
                        .withRecipientKey(recipientKey)
                        .build();

        assertThat(sample.getSenderKey()).isEqualTo(senderKey);
        assertThat(sample.getCipherText()).isEqualTo("cipherText".getBytes());
        assertThat(sample.getCipherTextNonce().getNonceBytes()).isEqualTo(cipherTextNonce);
        assertThat(sample.getRecipientNonce().getNonceBytes()).isEqualTo(recipientNonce);
        assertThat(sample.getRecipientBoxes()).hasSize(1).containsExactlyInAnyOrder(RecipientBox.from(recipientBox));
        assertThat(sample.getRecipientKeys()).hasSize(1).containsExactlyInAnyOrder(recipientKey);
        assertThat(sample.getAffectedContractTransactions()).hasSize(1);
        assertThat(sample.getAffectedContractTransactions().keySet())
                .containsExactly(
                        new TxHash(
                                "bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSw2J5hQ=="));
        assertThat(sample.getExecHash()).isEqualTo(execHash);
        assertThat(sample.getPrivacyMode()).isEqualTo(PrivacyMode.PRIVATE_STATE_VALIDATION);

        byte[] otherRecipientBox = "OTHETRBIX".getBytes();
        EncodedPayload fromSample = EncodedPayload.Builder.from(sample).withRecipientBox(otherRecipientBox).build();

        assertThat(fromSample.getRecipientBoxes()).hasSize(2)
            .containsExactly(RecipientBox.from(recipientBox),RecipientBox.from(otherRecipientBox));

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
                .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
                .withExecHash(execHash)
                .build();

        EncodedPayload result = EncodedPayload.Builder.from(sample).build();

        assertThat(result)
            .isNotSameAs(sample)
            .isEqualTo(sample);

        EqualsVerifier.forClass(EncodedPayload.class)
            .withIgnoredFields("affectedContractTransactions")
            .usingGetClass()
            .verify();

    }


}
