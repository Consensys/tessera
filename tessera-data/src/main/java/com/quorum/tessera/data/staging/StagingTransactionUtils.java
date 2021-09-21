package com.quorum.tessera.data.staging;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.EncodedPayloadCodec;
import com.quorum.tessera.enclave.PayloadDigest;
import com.quorum.tessera.enclave.PayloadEncoder;
import java.util.Base64;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class StagingTransactionUtils {

  private final PayloadDigest payloadDigest;

  private final PayloadEncoder payloadEncoder;

  private StagingTransactionUtils(PayloadDigest payloadDigest, PayloadEncoder payloadEncoder) {
    this.payloadDigest = Objects.requireNonNull(payloadDigest);
    this.payloadEncoder = Objects.requireNonNull(payloadEncoder);
  }

  public static StagingTransaction fromRawPayload(
      byte[] rawPayload, EncodedPayloadCodec encodedPayloadCodec) {
    PayloadDigest payloadDigest = PayloadDigest.create();
    PayloadEncoder payloadEncoder = PayloadEncoder.create(encodedPayloadCodec).get();
    return new StagingTransactionUtils(payloadDigest, payloadEncoder)
        .createFromRawPayload(rawPayload);
  }

  private StagingTransaction createFromRawPayload(byte[] rawPayload) {
    final EncodedPayload encodedPayload = payloadEncoder.decode(rawPayload);
    final byte[] messageHashData = payloadDigest.digest(encodedPayload.getCipherText());
    final String messageHash = Base64.getEncoder().encodeToString(messageHashData);

    StagingTransaction stagingTransaction = new StagingTransaction();
    stagingTransaction.setHash(messageHash);
    stagingTransaction.setPrivacyMode(encodedPayload.getPrivacyMode());
    stagingTransaction.setPayload(rawPayload);
    stagingTransaction.setEncodedPayloadCodec(payloadEncoder.encodedPayloadCodec());

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
