package com.quorum.tessera.data.staging;

import com.quorum.tessera.data.MessageHashFactory;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;

import java.util.Base64;
import java.util.Set;
import java.util.stream.Collectors;

public class StagingTransactionUtils {

    private static final MessageHashFactory MESSAGE_HASH_FACTORY = MessageHashFactory.create();

    private StagingTransactionUtils() {}

    public static StagingTransaction fromRawPayload(byte[] rawPayload) {

        final EncodedPayload encodedPayload = PayloadEncoder.create().decode(rawPayload);

        final byte[] messageHashData =
                MESSAGE_HASH_FACTORY.createFromCipherText(encodedPayload.getCipherText()).getHashBytes();
        final String messageHash = Base64.getEncoder().encodeToString(messageHashData);

        StagingTransaction stagingTransaction = new StagingTransaction();
        stagingTransaction.setHash(messageHash);
        stagingTransaction.setPrivacyMode(encodedPayload.getPrivacyMode());
        stagingTransaction.setPayload(rawPayload);

        final Set<StagingAffectedTransaction> affectedTransactions =
                encodedPayload.getAffectedContractTransactions().keySet().stream()
                        .map(key -> key.getBytes())
                        .map(Base64.getEncoder()::encodeToString)
                        .map(
                                hash -> {
                                    StagingAffectedTransaction stagingAffectedTransaction =
                                            new StagingAffectedTransaction();
                                    stagingAffectedTransaction.setHash(hash);
                                    stagingAffectedTransaction.setSourceTransaction(stagingTransaction);
                                    return stagingAffectedTransaction;
                                })
                        .collect(Collectors.toSet());

        stagingTransaction.setAffectedContractTransactions(affectedTransactions);

        return stagingTransaction;
    }
}
