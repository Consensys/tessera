package com.quorum.tessera.transaction;

import com.quorum.tessera.api.model.*;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.exception.OperationCurrentlySuspended;

import java.util.Objects;

/** Transaction manager wrapper that allows operations to be blocked/suspended. */
public class TransactionManagerWrapper implements TransactionManager {

    private final TransactionManager transactionManager;
    private volatile boolean resendMode = false;

    public TransactionManagerWrapper(TransactionManager transactionManager) {
        this.transactionManager = Objects.requireNonNull(transactionManager);
    }

    public void setResendMode(boolean value) {
        this.resendMode = value;
    }

    public TransactionManager getTransactionManager() {
        return this.transactionManager;
    }

    void checkAndThrow() {
        if (resendMode) {
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

    public MessageHash storePayloadBypass(byte[] payload) {
        return transactionManager.storePayload(payload);
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
}
