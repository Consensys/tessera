package com.quorum.tessera.client;

import com.quorum.tessera.api.model.ResendRequest;

public interface P2pClient {

    byte[] push(String targetUrl, byte[] data);

    boolean sendPartyInfo(String targetUrl, byte[] data);

    boolean makeResendRequest(String targetUrl, ResendRequest request);
 
}
