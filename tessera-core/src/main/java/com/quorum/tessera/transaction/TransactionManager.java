package com.quorum.tessera.transaction;

import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.PublicKey;
import java.util.List;

public interface TransactionManager {

  SendResponse send(SendRequest sendRequest);

  SendResponse sendSignedTransaction(SendSignedRequest sendRequest);

  void delete(MessageHash messageHash);

  MessageHash storePayload(EncodedPayload transactionPayload);

  ReceiveResponse receive(ReceiveRequest request);

  StoreRawResponse store(StoreRawRequest storeRequest);

  boolean upcheck();

  boolean isSender(MessageHash transactionHash);

  List<PublicKey> getParticipants(MessageHash transactionHash);

  /**
   * @see Enclave#defaultPublicKey()
   * @return
   */
  PublicKey defaultPublicKey();
}
