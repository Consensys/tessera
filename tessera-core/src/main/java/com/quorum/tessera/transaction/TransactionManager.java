package com.quorum.tessera.transaction;

import com.quorum.tessera.api.model.DeleteRequest;
import com.quorum.tessera.api.model.ReceiveRequest;
import com.quorum.tessera.api.model.ReceiveResponse;
import com.quorum.tessera.api.model.ResendRequest;
import com.quorum.tessera.api.model.SendRequest;
import com.quorum.tessera.api.model.SendResponse;
import java.util.Optional;


public interface TransactionManager {

    SendResponse send(SendRequest sendRequest);

    String receiveAndEncode(ReceiveRequest request);

    void delete(DeleteRequest request);

    Optional<byte[]> resendAndEncode(ResendRequest request);

    void storePayload(byte[] toByteArray);

    String storeAndEncodeKey(String sender, String recipientKeys, byte[] payload);

    void deleteKey(String key);

    ReceiveResponse receive(String hash, String toStr);

    byte[] receiveRaw(String hash, String recipientKey);

    

    
}
