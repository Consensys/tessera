package com.quorum.tessera.sync;

import com.quorum.tessera.partyinfo.ResendRequest;

/**
 * A client that can be used to make resend requests to other nodes. It cannot make requests to other endpoints and may
 * have different timeouts than P2P clients.
 */
public interface ResendClient {

    boolean makeResendRequest(String targetUrl, ResendRequest request);
}
