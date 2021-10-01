package com.quorum.tessera.enclave;

import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.service.Service;
import java.util.List;
import java.util.Objects;
import java.util.Set;

class EnclaveServerImpl implements EnclaveServer {

  private Enclave enclave;

  EnclaveServerImpl(Enclave enclave) {
    this.enclave = Objects.requireNonNull(enclave);
  }

  @Override
  public PublicKey defaultPublicKey() {
    return enclave.defaultPublicKey();
  }

  @Override
  public Set<PublicKey> getForwardingKeys() {
    return enclave.getForwardingKeys();
  }

  @Override
  public Set<PublicKey> getPublicKeys() {
    return enclave.getPublicKeys();
  }

  @Override
  public EncodedPayload encryptPayload(
      byte[] message,
      PublicKey senderPublicKey,
      List<PublicKey> recipientPublicKeys,
      PrivacyMetadata privacyMetadata) {
    return enclave.encryptPayload(message, senderPublicKey, recipientPublicKeys, privacyMetadata);
  }

  @Override
  public EncodedPayload encryptPayload(
      RawTransaction rawTransaction,
      List<PublicKey> recipientPublicKeys,
      PrivacyMetadata privacyMetadata) {
    return enclave.encryptPayload(rawTransaction, recipientPublicKeys, privacyMetadata);
  }

  @Override
  public Set<TxHash> findInvalidSecurityHashes(
      EncodedPayload encodedPayload, List<AffectedTransaction> affectedContractTransactions) {
    return enclave.findInvalidSecurityHashes(encodedPayload, affectedContractTransactions);
  }

  @Override
  public RawTransaction encryptRawPayload(byte[] message, PublicKey sender) {
    return enclave.encryptRawPayload(message, sender);
  }

  @Override
  public byte[] unencryptTransaction(EncodedPayload payload, PublicKey providedKey) {
    return enclave.unencryptTransaction(payload, providedKey);
  }

  @Override
  public byte[] unencryptRawPayload(RawTransaction payload) {
    return enclave.unencryptRawPayload(payload);
  }

  @Override
  public byte[] createNewRecipientBox(EncodedPayload payload, PublicKey recipientKey) {
    return enclave.createNewRecipientBox(payload, recipientKey);
  }

  @Override
  public void start() {
    enclave.start();
  }

  @Override
  public void stop() {
    enclave.stop();
  }

  @Override
  public Service.Status status() {
    return enclave.status();
  }
}
