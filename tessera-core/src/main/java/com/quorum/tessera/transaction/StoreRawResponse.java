package com.quorum.tessera.transaction;

import com.quorum.tessera.data.MessageHash;

public interface StoreRawResponse {

    MessageHash getHash();

    static StoreRawResponse from(MessageHash hash) {
        return new StoreRawResponse() {
            @Override
            public MessageHash getHash() {
                return hash;
            }
        };
    }

}
