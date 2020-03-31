package com.quorum.tessera.enclave;

import com.quorum.tessera.encryption.PublicKey;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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

        final EncodedPayload sample =
                EncodedPayload.Builder.create()
                        .withSenderKey(senderKey)
                        .withCipherText(cipherText)
                        .withCipherTextNonce(cipherTextNonce)
                        .withRecipientBoxes(Arrays.asList(recipientBox))
                        .withRecipientNonce(recipientNonce)
                        .withRecipientKeys(Arrays.asList(recipientKey))
                        .withPrivacyFlag(3)
                        .withAffectedContractTransactions(affectedContractTransactionsRaw)
                        .withExecHash(execHash)
                        .build();

        assertThat(sample.getSenderKey()).isEqualTo(senderKey);
        assertThat(sample.getCipherText()).isEqualTo("cipherText".getBytes());
        assertThat(sample.getCipherTextNonce().getNonceBytes()).isEqualTo(cipherTextNonce);
        assertThat(sample.getRecipientNonce().getNonceBytes()).isEqualTo(recipientNonce);
        assertThat(sample.getRecipientBoxes()).hasSize(1).containsExactlyInAnyOrder(recipientBox);
        assertThat(sample.getRecipientKeys()).hasSize(1).containsExactlyInAnyOrder(recipientKey);
        assertThat(sample.getAffectedContractTransactions()).hasSize(1);
        assertThat(sample.getAffectedContractTransactions().keySet())
                .containsExactly(
                        new TxHash(
                                "bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSw2J5hQ=="));
        assertThat(sample.getExecHash()).isEqualTo(execHash);
        assertThat(sample.getPrivacyMode()).isEqualTo(PrivacyMode.PRIVATE_STATE_VALIDATION);
    }
}
