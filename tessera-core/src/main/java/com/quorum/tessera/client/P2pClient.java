package com.quorum.tessera.client;

import com.quorum.tessera.api.model.ResendRequest;

public interface P2pClient {

    byte[] push(String targetUrl, byte[] data);

    byte[] getPartyInfo(String targetUrl, byte[] data);

    boolean makeResendRequest(final String targetUrl, final ResendRequest request);

    
    
}
