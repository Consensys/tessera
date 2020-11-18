package com.quorum.tessera.transaction;

import java.util.Optional;

enum EncodedPayloadManagerHolder {

    INSTANCE;

    private EncodedPayloadManager encodedPayloadManager;

    void storeInstance(EncodedPayloadManager encodedPayloadManager) {
        this.encodedPayloadManager = encodedPayloadManager;
    }

    Optional<EncodedPayloadManager> getEncodedPayloadManager() {
        return Optional.ofNullable(encodedPayloadManager);
    }

}
