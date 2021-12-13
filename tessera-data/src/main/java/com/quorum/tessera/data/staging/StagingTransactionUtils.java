package com.quorum.tessera.data.staging;

import com.quorum.tessera.enclave.*;
import java.util.Base64;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class StagingTransactionUtils {

  private final PayloadDigest payloadDigest;

  private StagingTransactionUtils(PayloadDigest payloadDigest) {
    this.payloadDigest = Objects.requireNonNull(payloadDigest);
  }

  public static StagingTransaction fromRawPayload(byte[] rawPayload, EncodedPayloadCodec codec) {
    PayloadDigest payloadDigest = PayloadDigest.create();
    PayloadEncoder encoder = PayloadEncoder.create(codec);
    return new StagingTransactionUtils(payloadDigest).createFromRawPayload(rawPayload, encoder);
  }

  private StagingTransaction createFromRawPayload(byte[] rawPayload, PayloadEncoder encoder) {
    final EncodedPayload encodedPayload = encoder.decode(rawPayload);
    final byte[] messageHashData = payloadDigest.digest(encodedPayload.getCipherText());
    final String messageHash = Base64.getEncoder().encodeToString(messageHashData);

    StagingTransaction stagingTransaction = new StagingTransaction();
    stagingTransaction.setHash(messageHash);
    stagingTransaction.setPrivacyMode(encodedPayload.getPrivacyMode());
    stagingTransaction.setPayload(rawPayload);
    stagingTransaction.setEncodedPayloadCodec(encoder.encodedPayloadCodec());

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
