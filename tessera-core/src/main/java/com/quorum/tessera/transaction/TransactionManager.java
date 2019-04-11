package com.quorum.tessera.transaction;

import com.quorum.tessera.api.model.*;
import com.quorum.tessera.enclave.model.MessageHash;


public interface TransactionManager {

    SendResponse send(SendRequest sendRequest);

    SendResponse sendSignedTransaction(SendSignedRequest sendRequest);

    void delete(DeleteRequest request);

    ResendResponse resend(ResendRequest request);

    MessageHash storePayload(byte[] toByteArray);

    ReceiveResponse receive(ReceiveRequest request);
    
    StoreRawResponse store(StoreRawRequest storeRequest);

    
}
