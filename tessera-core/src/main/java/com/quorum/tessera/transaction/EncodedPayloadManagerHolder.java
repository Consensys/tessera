package com.quorum.tessera.transaction;

import java.util.Optional;

enum EncodedPayloadManagerHolder {

    INSTANCE;

    private EncodedPayloadManager encodedPayloadManager;

    public void storeInstance(EncodedPayloadManager encodedPayloadManager) {
        this.encodedPayloadManager = encodedPayloadManager;
    }

    public Optional<EncodedPayloadManager> getEncodedPayloadManager() {
        return Optional.ofNullable(encodedPayloadManager);
    }

    public static EncodedPayloadManagerHolder getInstance() {
        return INSTANCE;
    }

}
