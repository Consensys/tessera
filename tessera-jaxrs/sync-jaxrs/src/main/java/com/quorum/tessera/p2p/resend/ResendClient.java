package com.quorum.tessera.p2p.resend;

import com.quorum.tessera.p2p.recovery.PushBatchRequest;
import com.quorum.tessera.p2p.recovery.ResendBatchRequest;
import com.quorum.tessera.p2p.recovery.ResendBatchResponse;

/**
 * A client that can be used to make resend requests to other nodes. It cannot make requests to other endpoints and may
 * have different timeouts than P2P clients.
 */
public interface ResendClient {

    boolean makeResendRequest(String targetUrl, ResendRequest request);

    //    boolean pushBatch(String targetUrl, PushBatchRequest request);
    //
    //    ResendBatchResponse makeBatchResendRequest(String targetUrl, ResendBatchRequest request);
}
