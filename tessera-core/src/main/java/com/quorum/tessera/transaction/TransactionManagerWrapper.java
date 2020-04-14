package com.quorum.tessera.transaction;

import com.quorum.tessera.api.model.*;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.exception.OperationCurrentlySuspended;
import com.quorum.tessera.partyinfo.ResendRequest;
import com.quorum.tessera.partyinfo.ResendResponse;
import com.quorum.tessera.sync.ResendStoreDelegate;

import java.util.Objects;

/** Transaction manager wrapper that allows operations to be blocked/suspended. */
public class TransactionManagerWrapper implements TransactionManager, ResendStoreDelegate {

    private final TransactionManager transactionManager;

    private final SyncState syncState;

    public TransactionManagerWrapper(TransactionManager transactionManager, SyncState syncState) {
        this.transactionManager = Objects.requireNonNull(transactionManager);
        this.syncState = Objects.requireNonNull(syncState);
    }

    void checkAndThrow() {
        if (syncState.isResendMode()) {
            throw new OperationCurrentlySuspended("Operation is currently suspended.");
        }
    }

    @Override
    public SendResponse send(SendRequest sendRequest) {
        checkAndThrow();
        return transactionManager.send(sendRequest);
    }

    @Override
    public SendResponse sendSignedTransaction(SendSignedRequest sendRequest) {
        checkAndThrow();
        return transactionManager.sendSignedTransaction(sendRequest);
    }

    @Override
    public void delete(DeleteRequest request) {
        checkAndThrow();
        transactionManager.delete(request);
    }

    @Override
    public ResendResponse resend(ResendRequest request) {
        checkAndThrow();
        return transactionManager.resend(request);
    }

    @Override
    public MessageHash storePayload(byte[] toByteArray) {
        checkAndThrow();
        return transactionManager.storePayload(toByteArray);
    }

    @Override
    public ReceiveResponse receive(ReceiveRequest request) {
        checkAndThrow();
        return transactionManager.receive(request);
    }

    @Override
    public StoreRawResponse store(StoreRawRequest storeRequest) {
        checkAndThrow();
        return transactionManager.store(storeRequest);
    }

    @Override
    public MessageHash storePayloadBypassResendMode(byte[] payload) {
        return transactionManager.storePayload(payload);
    }
}
