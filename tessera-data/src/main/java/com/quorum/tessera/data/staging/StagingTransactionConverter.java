package com.quorum.tessera.data.staging;

import com.quorum.tessera.data.MessageHashFactory;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoderImpl;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.enclave.TxHash;
import com.quorum.tessera.encryption.PublicKey;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class StagingTransactionConverter {

    private StagingTransactionConverter() {}

    public static StagingTransaction fromRawPayload(final byte[] payload) {
        final EncodedPayload encodedPayload = new PayloadEncoderImpl().decode(payload);

        final MessageHashStr messageHash =
                new MessageHashStr(
                        MessageHashFactory.create()
                                .createFromCipherText(encodedPayload.getCipherText())
                                .getHashBytes());
        final StagingTransaction stagingTransaction = new StagingTransaction();
        stagingTransaction.setHash(messageHash);
        stagingTransaction.setCipherText(encodedPayload.getCipherText());
        stagingTransaction.setCipherTextNonce(encodedPayload.getCipherTextNonce().getNonceBytes());
        stagingTransaction.setRecipientNonce(encodedPayload.getRecipientNonce().getNonceBytes());
        stagingTransaction.setSenderKey(encodedPayload.getSenderKey().getKeyBytes());
        stagingTransaction.setExecHash(encodedPayload.getExecHash());
        stagingTransaction.setPrivacyMode(encodedPayload.getPrivacyMode());

        PublicKey firstRecipientKey = encodedPayload.getRecipientKeys().get(0);
        final StagingRecipient firstStagingRecipient = new StagingRecipient(firstRecipientKey.getKeyBytes());

        stagingTransaction.getRecipients().add(firstStagingRecipient);

        for (PublicKey recipient : encodedPayload.getRecipientKeys()
            .subList(1,encodedPayload.getRecipientKeys().size())) {

            final StagingRecipient stagingRecipient = new StagingRecipient(recipient.getKeyBytes());

            stagingRecipient.setMessageHash(messageHash);
            stagingRecipient.setTransaction(stagingTransaction);
            stagingRecipient.setInitiator(false);
            stagingTransaction.getRecipients().add(stagingRecipient);
        }

        for (Map.Entry<TxHash, byte[]> entry : encodedPayload.getAffectedContractTransactions().entrySet()) {
            final StagingAffectedContractTransactionId affectedContractTransactionId =
                    new StagingAffectedContractTransactionId(
                            messageHash, new MessageHashStr(entry.getKey().getBytes()));
            final StagingAffectedContractTransaction stagingAffectedContractTransaction =
                    new StagingAffectedContractTransaction();
            stagingAffectedContractTransaction.setSecurityHash(entry.getValue());
            stagingAffectedContractTransaction.setSourceTransaction(stagingTransaction);
            stagingAffectedContractTransaction.setStagingAffectedContractTransactionId(affectedContractTransactionId);
            stagingTransaction
                    .getAffectedContractTransactions()
                    .put(affectedContractTransactionId.getAffected(), stagingAffectedContractTransaction);
        }

        StagingTransactionVersion version = new StagingTransactionVersion();
        version.setTransaction(stagingTransaction);
        version.setPayload(payload);

        version.setPrivacyMode(stagingTransaction.getPrivacyMode());
        stagingTransaction.getVersions().add(version);

        return stagingTransaction;
    }

    public static StagingTransaction versionStagingTransaction(
            StagingTransaction existing, StagingTransaction newTransaction) {

        if (!compareData(existing, newTransaction)) {
            existing.setIssues("Data mismatched across versions");
        }

        if (PrivacyMode.PRIVATE_STATE_VALIDATION == existing.getPrivacyMode()) {
            if (!(existing.getRecipients().containsAll(newTransaction.getRecipients())
                    && newTransaction.getRecipients().containsAll(existing.getRecipients()))) {
                existing.setIssues("Recipients mismatched across versions");
            }
        } else {
            existing.getRecipients().addAll(newTransaction.getRecipients());
        }
        existing.getAffectedContractTransactions().putAll(newTransaction.getAffectedContractTransactions());

        Set<StagingTransactionVersion> versions = newTransaction.getVersions();
        existing.getVersions().addAll(versions);

        return existing;
    }

    private static boolean compareData(StagingTransaction st1, StagingTransaction st2) {
        return Arrays.equals(st1.getSenderKey(), st2.getSenderKey())
                && Arrays.equals(st1.getCipherText(), st2.getCipherText())
                && Arrays.equals(st1.getCipherTextNonce(), st2.getCipherTextNonce())
                && Arrays.equals(st1.getRecipientNonce(), st2.getRecipientNonce())
                && Arrays.equals(st1.getExecHash(), st2.getExecHash())
                && st1.getPrivacyMode() == st2.getPrivacyMode();
    }
}
