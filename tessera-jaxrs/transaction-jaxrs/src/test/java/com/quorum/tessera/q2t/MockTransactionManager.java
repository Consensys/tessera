package com.quorum.tessera.q2t;

import com.quorum.tessera.api.model.*;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.ResendRequest;
import com.quorum.tessera.partyinfo.ResendResponse;
import com.quorum.tessera.transaction.TransactionManager;

import java.util.List;

public class MockTransactionManager implements TransactionManager {
    @Override
    public SendResponse send(SendRequest sendRequest) {
        return null;
    }

    @Override
    public SendResponse sendSignedTransaction(SendSignedRequest sendRequest) {
        return null;
    }

    @Override
    public void delete(DeleteRequest request) {

    }

    @Override
    public ResendResponse resend(ResendRequest request) {
        return null;
    }

    @Override
    public MessageHash storePayload(byte[] toByteArray) {
        return null;
    }

    @Override
    public ReceiveResponse receive(ReceiveRequest request) {
        return null;
    }

    @Override
    public StoreRawResponse store(StoreRawRequest storeRequest) {
        return null;
    }

    @Override
    public boolean isSender(String ptmHash) {
        return false;
    }

    @Override
    public List<PublicKey> getParticipants(String ptmHash) {
        return null;
    }
}
