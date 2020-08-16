package com.quorum.tessera.p2p;

/**
 * A client that can be used to make resend requests to other nodes. It cannot make requests to other endpoints and may
 * have different timeouts than P2P clients.
 */
public interface ResendClient<T> {

    boolean makeResendRequest(String targetUrl, T request);
}
