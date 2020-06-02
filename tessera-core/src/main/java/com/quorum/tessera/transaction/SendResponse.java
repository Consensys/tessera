package com.quorum.tessera.transaction;

import com.quorum.tessera.data.MessageHash;

public interface SendResponse {

    MessageHash getTransactionHash();

    static SendResponse from(MessageHash messageHash) {
        return new SendResponse() {
            @Override
            public MessageHash getTransactionHash() {
                return messageHash;
            }
        };
    }

}
