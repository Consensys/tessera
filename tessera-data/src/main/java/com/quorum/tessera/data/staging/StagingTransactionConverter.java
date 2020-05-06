package com.quorum.tessera.data.staging;

import com.quorum.tessera.data.MessageHashFactory;
import com.quorum.tessera.enclave.EncodedPayload;

import java.util.Arrays;
import java.util.Base64;
import java.util.Set;
import java.util.stream.Collectors;

public class StagingTransactionConverter {

    private static final MessageHashFactory MESSAGE_HASH_FACTORY = MessageHashFactory.create();

    private StagingTransactionConverter() {}

    public static Set<StagingTransaction> fromPayload(EncodedPayload encodedPayload) {

        final Set<StagingAffectedTransaction> affectedTransactions =
            encodedPayload.getAffectedContractTransactions().keySet()
            .stream()
            .map(key -> key.getBytes())
            .map(Base64.getEncoder()::encodeToString)
            .map(messageHash -> {
                StagingAffectedTransaction stagingAffectedTransaction = new StagingAffectedTransaction();
                stagingAffectedTransaction.setHash(messageHash);
                return stagingAffectedTransaction;
            }).collect(Collectors.toSet());

        final byte[] messageHashData = MESSAGE_HASH_FACTORY.createFromCipherText(encodedPayload.getCipherText()).getHashBytes();

        final String messageHash = Base64.getEncoder().encodeToString(messageHashData);

        return encodedPayload.getRecipientKeys().stream().map(recipientKey -> {

            StagingTransaction stagingTransaction = new StagingTransaction();
            stagingTransaction.setHash(messageHash);
            stagingTransaction.setSenderKey(encodedPayload.getSenderKey().getKeyBytes());
            stagingTransaction.setCipherText(encodedPayload.getCipherText());
            stagingTransaction.setCipherTextNonce(encodedPayload.getCipherTextNonce().getNonceBytes());
            stagingTransaction.setRecipientNonce(encodedPayload.getRecipientNonce().getNonceBytes());
            stagingTransaction.setExecHash(encodedPayload.getExecHash());
            stagingTransaction.setPrivacyMode(encodedPayload.getPrivacyMode());
            stagingTransaction.setRecipientKey(recipientKey.getKeyBytes());
            stagingTransaction.setAffectedContractTransactions(affectedTransactions);

            return stagingTransaction;
        }).collect(Collectors.toSet());


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
