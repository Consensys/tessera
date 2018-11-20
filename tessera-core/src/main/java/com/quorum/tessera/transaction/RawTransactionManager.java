package com.quorum.tessera.transaction;

import com.quorum.tessera.api.model.StoreRawRequest;
import com.quorum.tessera.api.model.StoreRawResponse;


public interface RawTransactionManager {

    StoreRawResponse store(StoreRawRequest storeRequest);
}
