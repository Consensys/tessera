package com.quorum.tessera.p2p;


import com.quorum.tessera.config.Config;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.transaction.*;

import java.util.List;
import java.util.Optional;

public class MockTransactionManager implements TransactionManager, TransactionManagerFactory {

    @Override
    public SendResponse send(SendRequest sendRequest) {
        return null;
    }

    @Override
    public SendResponse sendSignedTransaction(SendSignedRequest sendRequest) {
        return null;
    }

    @Override
    public void delete(MessageHash messageHash) {}

    @Override
    public com.quorum.tessera.transaction.ResendResponse resend(com.quorum.tessera.transaction.ResendRequest request) {
        return null;
    }

    @Override
    public MessageHash storePayload(EncodedPayload transactionPayload) {
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
    public boolean isSender(MessageHash transactionHash) {
        return false;
    }

    @Override
    public List<PublicKey> getParticipants(MessageHash transactionHash) {
        return null;
    }

    @Override
    public PublicKey defaultPublicKey() {
        return null;
    }

    @Override
    public TransactionManager create(Config config) {
        return this;
    }

    @Override
    public Optional<TransactionManager> transactionManager() {
        return Optional.of(this);
    }
}
