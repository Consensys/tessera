package com.quorum.tessera.transaction;

import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.PublicKey;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

public interface TransactionManager {

  SendResponse send(SendRequest sendRequest);

  SendResponse sendSignedTransaction(SendSignedRequest sendRequest);

  void delete(MessageHash messageHash);

  void deleteAll(PublicKey key);

  MessageHash storePayload(EncodedPayload transactionPayload);

  ReceiveResponse receive(ReceiveRequest request);

  StoreRawResponse store(StoreRawRequest storeRequest);

  boolean upcheck();

  boolean isSender(MessageHash transactionHash);

  List<PublicKey> getParticipants(MessageHash transactionHash);

  Set<PublicKey> getMandatoryRecipients(MessageHash transactionHash);

  /**
   * @see Enclave#defaultPublicKey()
   * @return
   */
  PublicKey defaultPublicKey();

  static TransactionManager create() {
    return ServiceLoader.load(TransactionManager.class).findFirst().get();
  }
}
