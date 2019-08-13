package com.quorum.tessera.partyinfo;

public interface P2pClient {

    byte[] push(String targetUrl, byte[] data);

    boolean sendPartyInfo(String targetUrl, byte[] data);

    boolean makeResendRequest(String targetUrl, Object request);

    boolean pushBatch(String targetUrl, PushBatchRequest request);

    ResendBatchResponse makeBatchResendRequest(String targetUrl, ResendBatchRequest request);
}
