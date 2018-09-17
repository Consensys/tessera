
package com.quorum.tessera.client;

import com.quorum.tessera.api.model.ApiPath;
import com.quorum.tessera.api.model.ResendRequest;
import java.util.Objects;

public class RestP2pClient implements P2pClient {
    
    private final PostDelegate postDelegate;

    public RestP2pClient(PostDelegate postDelegate) {
        this.postDelegate = Objects.requireNonNull(postDelegate);
    }

    @Override
    public byte[] push(String targetUrl, byte[] data) {
        return postDelegate.doPost(targetUrl, ApiPath.PUSH, data);
    }

    @Override
    public byte[] getPartyInfo(String targetUrl, byte[] data) {
       return postDelegate.doPost(targetUrl, ApiPath.PARTYINFO, data);
    }

    @Override
    public boolean makeResendRequest(String targetUrl, ResendRequest request) {
        return postDelegate.makeResendRequest(targetUrl, request);
    }

    
}
