package com.quorum.tessera.transaction;

import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.PublicKey;

import java.util.List;
import java.util.ServiceLoader;

public interface TransactionManager {

    SendResponse send(SendRequest sendRequest);

    SendResponse sendSignedTransaction(SendSignedRequest sendRequest);

    void delete(MessageHash messageHash);

    ResendResponse resend(ResendRequest request);

    MessageHash storePayload(EncodedPayload transactionPayload);

    ReceiveResponse receive(ReceiveRequest request);

    StoreRawResponse store(StoreRawRequest storeRequest);

    boolean isSender(MessageHash transactionHash);

    List<PublicKey> getParticipants(MessageHash transactionHash);

    /**
     * @see Enclave#defaultPublicKey()
     * @return
     */
    PublicKey defaultPublicKey();


    static TransactionManager create() {
        return ServiceLoader.load(TransactionManager.class).findFirst().get();
    }


}
