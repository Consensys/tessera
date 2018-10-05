package com.quorum.tessera.transaction;

import com.quorum.tessera.api.model.DeleteRequest;
import com.quorum.tessera.api.model.ReceiveRequest;
import com.quorum.tessera.api.model.ReceiveResponse;
import com.quorum.tessera.api.model.ResendRequest;
import com.quorum.tessera.api.model.ResendResponse;
import com.quorum.tessera.api.model.SendRequest;
import com.quorum.tessera.api.model.SendResponse;


public interface TransactionManager {

    SendResponse send(SendRequest sendRequest);

    void delete(DeleteRequest request);

    ResendResponse resend(ResendRequest request);

    void storePayload(byte[] toByteArray);

    ReceiveResponse receive(ReceiveRequest request);

    
}
