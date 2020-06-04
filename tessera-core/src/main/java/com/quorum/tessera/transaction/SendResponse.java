package com.quorum.tessera.transaction;

import com.quorum.tessera.data.MessageHash;

import java.util.Objects;

public interface SendResponse {

    MessageHash getTransactionHash();

    static SendResponse from(MessageHash messageHash) {
        Objects.requireNonNull(messageHash, "Transaction hash is required");

        return () -> messageHash;
    }
}
