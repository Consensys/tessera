package com.quorum.tessera.data;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoderImpl;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.enclave.TxHash;
import com.quorum.tessera.encryption.PublicKey;

import java.util.Arrays;
import java.util.Map;

public class StagingTransactionConverter {

    private StagingTransactionConverter() {
    }

    public static StagingTransaction fromRawPayload(final byte[] payload) {
        final EncodedPayload encodedPayload = new PayloadEncoderImpl().decode(payload);

        final MessageHashStr messageHash = new MessageHashStr(MessageHashFactory.create()
            .createFromCipherText(encodedPayload.getCipherText()).getHashBytes());
        final StagingTransaction stagingTransaction = new StagingTransaction();
        stagingTransaction.setHash(messageHash);
        stagingTransaction.setCipherText(encodedPayload.getCipherText());
        stagingTransaction.setCipherTextNonce(encodedPayload.getCipherTextNonce().getNonceBytes());
        stagingTransaction.setRecipientNonce(encodedPayload.getRecipientNonce().getNonceBytes());
        stagingTransaction.setSenderKey(encodedPayload.getSenderKey().getKeyBytes());
        stagingTransaction.setExecHash(encodedPayload.getExecHash());
        stagingTransaction.setPrivacyMode((byte) encodedPayload.getPrivacyMode().getPrivacyFlag());

        for (PublicKey recipient : encodedPayload.getRecipientKeys()) {
            final StagingRecipient stagingRecipient = new StagingRecipient(recipient.getKeyBytes());
            final StagingTransactionRecipientId stagingTransactionRecipientId = new StagingTransactionRecipientId(messageHash, stagingRecipient);
            final StagingTransactionRecipient stagingTransactionRecipient = new StagingTransactionRecipient();
            stagingTransactionRecipient.setId(stagingTransactionRecipientId);
            stagingTransactionRecipient.setTransaction(stagingTransaction);
            stagingTransactionRecipient.setInitiator(false);
            stagingTransaction.getRecipients().put(stagingRecipient, stagingTransactionRecipient);
        }

        final StagingRecipient stagingRecipient = new StagingRecipient(encodedPayload.getRecipientKeys().get(0).getKeyBytes());

        stagingTransaction.getRecipients().get(stagingRecipient).setBox(encodedPayload.getRecipientBoxes().get(0));

        for (Map.Entry<TxHash, byte[]> entry : encodedPayload.getAffectedContractTransactions().entrySet()) {
            final StagingAffectedContractTransactionId affectedContractTransactionId =
                new StagingAffectedContractTransactionId(messageHash, new MessageHashStr(entry.getKey().getBytes()));
            final StagingAffectedContractTransaction stagingAffectedContractTransaction = new StagingAffectedContractTransaction();
            stagingAffectedContractTransaction.setSecurityHash(entry.getValue());
            stagingAffectedContractTransaction.setSourceTransaction(stagingTransaction);
            stagingAffectedContractTransaction.setId(affectedContractTransactionId);
            stagingTransaction.getAffectedContractTransactions().put(affectedContractTransactionId.getAffected(),
                stagingAffectedContractTransaction);
        }

        StagingTransactionVersion version = new StagingTransactionVersion();
        version.setTransaction(stagingTransaction);
        version.setPayload(payload);
        version.setId(new StagingTransactionRecipientId(messageHash, stagingRecipient));
        version.setPrivacyMode(stagingTransaction.getPrivacyMode());
        stagingTransaction.getVersions().put(stagingRecipient, version);

        return stagingTransaction;
    }

    public static StagingTransaction versionStagingTransaction(StagingTransaction existing, StagingTransaction newTransaction) {

        if (!compareData(existing, newTransaction)) {
            existing.setIssues("Data mismatched across versions");
        }

        if (PrivacyMode.PRIVATE_STATE_VALIDATION == PrivacyMode.fromFlag(existing.getPrivacyMode())) {
            if (!(existing.getRecipients().keySet().containsAll(newTransaction.getRecipients().keySet())
                && newTransaction.getRecipients().keySet().containsAll(existing.getRecipients().keySet()))) {
                existing.setIssues("Recipients mismatched across versions");
            }
        } else {
            existing.getRecipients().putAll(newTransaction.getRecipients());
        }
        existing.getAffectedContractTransactions().putAll(newTransaction.getAffectedContractTransactions());
        existing.getVersions().putAll(newTransaction.getVersions());

        return existing;
    }

    private static boolean compareData(StagingTransaction st1, StagingTransaction st2) {
        return
            Arrays.equals(st1.getSenderKey(), st2.getSenderKey()) &&
                Arrays.equals(st1.getCipherText(), st2.getCipherText()) &&
                Arrays.equals(st1.getCipherTextNonce(), st2.getCipherTextNonce()) &&
                Arrays.equals(st1.getRecipientNonce(), st2.getRecipientNonce()) &&
                Arrays.equals(st1.getExecHash(), st2.getExecHash()) &&
                st1.getPrivacyMode() == st2.getPrivacyMode();
    }
}
