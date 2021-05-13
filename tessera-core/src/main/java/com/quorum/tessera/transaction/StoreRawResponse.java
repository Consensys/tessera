package com.quorum.tessera.transaction;

import com.quorum.tessera.data.MessageHash;
import java.util.Objects;

public interface StoreRawResponse {

  MessageHash getHash();

  static StoreRawResponse from(MessageHash hash) {
    Objects.requireNonNull(hash, "Transaction hash is required");
    return () -> hash;
  }
}
