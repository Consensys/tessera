package com.quorum.tessera.data.staging;

import com.quorum.tessera.enclave.*;
import com.quorum.tessera.encryption.Nonce;
import com.quorum.tessera.encryption.PublicKey;
import org.junit.Test;

import java.util.*;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class StagingTransactionUtilsTest {

    private final PublicKey sender = PublicKey.from("sender".getBytes());

    private final PublicKey recipient1 = PublicKey.from("recipient1".getBytes());

    private final PayloadEncoder encoder = new PayloadEncoderImpl();

    @Test
    public void testFromRawPayload() {

        final TxHash affectedHash = new TxHash("TX2");

        final EncodedPayload encodedPayload =
                EncodedPayload.Builder.create()
                        .withSenderKey(sender)
                        .withCipherText("cipherText".getBytes())
                        .withCipherTextNonce(new Nonce("nonce".getBytes()))
                        .withRecipientBoxes(Arrays.asList("box1".getBytes(), "box2".getBytes()))
                        .withRecipientNonce(new Nonce("recipientNonce".getBytes()))
                        .withRecipientKeys(Arrays.asList(recipient1))
                        .withPrivacyMode(PrivacyMode.PARTY_PROTECTION)
                        .withAffectedContractTransactions(singletonMap(affectedHash, "somesecurityHash".getBytes()))
                        .withExecHash("execHash".getBytes())
                        .build();

        final String messageHash =
            Base64.getEncoder().encodeToString(new PayloadDigest.Default().digest(encodedPayload.getCipherText()));

        final byte[] raw = encoder.encode(encodedPayload);

        StagingTransaction result = StagingTransactionUtils.fromRawPayload(raw);

        assertThat(result).isNotNull();
        assertThat(result.getHash()).isEqualTo(messageHash);
        assertThat(result.getPayload()).isEqualTo(raw);
        assertThat(result.getPrivacyMode()).isEqualTo(PrivacyMode.PARTY_PROTECTION);
        assertThat(result.getValidationStage()).isNull();
        assertThat(result.getAffectedContractTransactions()).hasSize(1);

        result.getAffectedContractTransactions()
                .forEach(
                        atx -> {
                            assertThat(atx.getHash()).isEqualTo(affectedHash.encodeToBase64());
                            assertThat(atx.getSourceTransaction()).isEqualTo(result);
                        });
    }
}
