package com.quorum.tessera.data;

import com.quorum.tessera.enclave.*;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.nacl.Nonce;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class StagingTransactionConverterTest {

    private final PublicKey sender = PublicKey.from("sender".getBytes());

    private final PublicKey recipient1 = PublicKey.from("recipient1".getBytes());
    private final PublicKey recipient2 = PublicKey.from("recipient2".getBytes());

    private final PayloadEncoder encoder = new PayloadEncoderImpl();

    @Test
    public void testConvertAndVersionStagingTransaction() {

        final EncodedPayload originalPayload1 = new EncodedPayload(
            sender,
            "cipherText".getBytes(),
            new Nonce("nonce".getBytes()),
            Arrays.asList("box1".getBytes(), "box2".getBytes()),
            new Nonce("recipientNonce".getBytes()),
            Arrays.asList(recipient1, recipient2),
            PrivacyMode.PARTY_PROTECTION,
            emptyMap(),
            "execHash".getBytes()
        );

        final TxHash txHash1 =
            new TxHash(MessageHashFactory.create()
                .createFromCipherText(originalPayload1.getCipherText()).getHashBytes());
        final byte[] encodedRaw1 = encoder.encode(originalPayload1);

        final Map<TxHash, byte[]> affectedTx = new HashMap<>();
        affectedTx.put(txHash1, encodedRaw1);

        final EncodedPayload originalPayload2 = new EncodedPayload(
            sender,
            "cipherText".getBytes(),
            new Nonce("nonce".getBytes()),
            Arrays.asList("box1".getBytes(), "box2".getBytes()),
            new Nonce("recipientNonce".getBytes()),
            Arrays.asList(recipient1, recipient2),
            PrivacyMode.PARTY_PROTECTION,
            affectedTx,
            "execHash".getBytes()
        );

        final TxHash txHash2 =
            new TxHash(MessageHashFactory.create()
                .createFromCipherText(originalPayload2.getCipherText()).getHashBytes());

        final EncodedPayload payload1Recipient1 = new EncodedPayload(
            sender,
            "cipherText".getBytes(),
            new Nonce("nonce".getBytes()),
            singletonList("box1".getBytes()),
            new Nonce("recipientNonce".getBytes()),
            singletonList(recipient1),
            PrivacyMode.PARTY_PROTECTION,
            emptyMap(),
            "execHash".getBytes()
        );

        final EncodedPayload payload1Recipient2 = new EncodedPayload(
            sender,
            "cipherText".getBytes(),
            new Nonce("nonce".getBytes()),
            singletonList("box2".getBytes()),
            new Nonce("recipientNonce".getBytes()),
            singletonList(recipient2),
            PrivacyMode.PARTY_PROTECTION,
            emptyMap(),
            "execHash".getBytes()
        );

        final EncodedPayload payload2Recipient1 = new EncodedPayload(
            sender,
            "cipherText".getBytes(),
            new Nonce("nonce".getBytes()),
            singletonList("box1".getBytes()),
            new Nonce("recipientNonce".getBytes()),
            singletonList(recipient1),
            PrivacyMode.PARTY_PROTECTION,
            affectedTx,
            "execHash".getBytes()
        );

        final EncodedPayload payload2Recipient2 = new EncodedPayload(
            sender,
            "cipherText".getBytes(),
            new Nonce("nonce".getBytes()),
            singletonList("box2".getBytes()),
            new Nonce("recipientNonce".getBytes()),
            singletonList(recipient2),
            PrivacyMode.PARTY_PROTECTION,
            affectedTx,
            "execHash".getBytes()
        );

        final StagingTransaction stagingTransaction1
            = StagingTransactionConverter.fromRawPayload(encoder.encode(payload2Recipient1));

        final StagingTransaction stagingTransaction2
            = StagingTransactionConverter.fromRawPayload(encoder.encode(payload2Recipient2));

        final StagingTransaction stagingTransaction3
            = StagingTransactionConverter.fromRawPayload(encoder.encode(payload1Recipient1));

        final StagingTransaction stagingTransaction4
            = StagingTransactionConverter.fromRawPayload(encoder.encode(payload1Recipient2));

        final StagingTransaction mergedTransaction = StagingTransactionConverter
            .versionStagingTransaction(stagingTransaction1, stagingTransaction2);

        final StagingTransaction mergedAffectedTx = StagingTransactionConverter
            .versionStagingTransaction(stagingTransaction3, stagingTransaction4);

        assertThat(mergedTransaction).isNotNull();
        assertThat(mergedTransaction.getSenderKey()).isEqualTo(sender.getKeyBytes());
        assertThat(mergedTransaction.getHash().getHashBytes()).isEqualTo(txHash2.getBytes());
        assertThat(mergedTransaction.getPrivacyMode()).isEqualTo((byte)1);
        assertThat(mergedTransaction.getCipherText()).isEqualTo("cipherText".getBytes());
        assertThat(mergedTransaction.getCipherTextNonce()).isEqualTo("nonce".getBytes());
        assertThat(mergedTransaction.getRecipientNonce()).isEqualTo("recipientNonce".getBytes());
        assertThat(mergedTransaction.getExecHash()).isEqualTo("execHash".getBytes());
        assertThat(mergedTransaction.getAffectedContractTransactions().size()).isEqualTo(1);

        final StagingAffectedContractTransactionId id =
            mergedTransaction.getAffectedContractTransactions().values()
                .stream()
                .map(StagingAffectedContractTransaction::getId)
                .collect(Collectors.toList())
                .get(0);

        assertThat(id.getSource()).isEqualTo(mergedTransaction.getHash());
        assertThat(id.getAffected()).isEqualTo(mergedAffectedTx.getHash());

        assertThat(mergedTransaction.getRecipients().size()).isEqualTo(2);
        assertThat(mergedTransaction.getRecipients().keySet())
        .containsAll(
            Arrays.asList(
                new StagingRecipient(recipient1.getKeyBytes()),
                new StagingRecipient(recipient2.getKeyBytes())));


        assertThat(mergedTransaction.getVersions().size()).isEqualTo(2);
        final List<byte[]> payloads = mergedTransaction
            .getVersions().values()
            .stream().map(StagingTransactionVersion::getPayload)
            .collect(Collectors.toList());
        assertThat(payloads).containsExactlyInAnyOrder(
            encoder.encode(payload2Recipient1),
            encoder.encode(payload2Recipient2));


    }

    @Test
    public void testDataConsistencyIssue() {

        StagingTransaction st1 = new StagingTransaction();
        StagingTransaction st2 = new StagingTransaction();

        st1.setSenderKey("key".getBytes());
        st2.setSenderKey("bogus".getBytes());
        assertThat(StagingTransactionConverter.versionStagingTransaction(st1, st2).getIssues())
            .isEqualTo("Data mismatched across versions");
        st2.setSenderKey("key".getBytes());
        st1.setIssues("");

        st1.setCipherText("cipherText".getBytes());
        st2.setCipherText("bogus".getBytes());
        assertThat(StagingTransactionConverter.versionStagingTransaction(st1, st2).getIssues())
            .isEqualTo("Data mismatched across versions");
        st2.setCipherText("cipherText".getBytes());
        st1.setIssues("");

        st1.setCipherTextNonce("ctNonce".getBytes());
        st2.setCipherTextNonce("bogus".getBytes());
        assertThat(StagingTransactionConverter.versionStagingTransaction(st1, st2).getIssues())
            .isEqualTo("Data mismatched across versions");
        st2.setCipherTextNonce("ctNonce".getBytes());
        st1.setIssues("");

        st1.setRecipientNonce("rNonce".getBytes());
        st2.setRecipientNonce("bogus".getBytes());
        assertThat(StagingTransactionConverter.versionStagingTransaction(st1, st2).getIssues())
            .isEqualTo("Data mismatched across versions");
        st2.setRecipientNonce("rNonce".getBytes());
        st1.setIssues("");

        st1.setExecHash("execHash".getBytes());
        st2.setExecHash("bogus".getBytes());
        assertThat(StagingTransactionConverter.versionStagingTransaction(st1, st2).getIssues())
            .isEqualTo("Data mismatched across versions");
        st2.setExecHash("execHash".getBytes());
        st1.setIssues("");

        st1.setPrivacyMode((byte) 3);
        st2.setPrivacyMode((byte) 1);
        assertThat(StagingTransactionConverter.versionStagingTransaction(st1, st2).getIssues())
            .isEqualTo("Data mismatched across versions");
        st2.setPrivacyMode((byte) 3);
        st1.setIssues("");

    }

    @Test
    public void testRecipientsMismatchedForPsvTransaction() {
        StagingTransaction st1 = new StagingTransaction();
        StagingTransaction st2 = new StagingTransaction();

        st1.setPrivacyMode((byte)3);
        st2.setPrivacyMode((byte)1);

        st1.getRecipients().put(new StagingRecipient("key".getBytes()), new StagingTransactionRecipient());

        assertThat(StagingTransactionConverter.versionStagingTransaction(st1, st2).getIssues())
            .isEqualTo("Recipients mismatched across versions");

    }
}
