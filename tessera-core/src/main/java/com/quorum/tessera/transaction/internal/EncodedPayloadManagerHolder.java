package com.quorum.tessera.transaction.internal;

import com.quorum.tessera.transaction.EncodedPayloadManager;
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
