
package com.quorum.tessera.client;

import com.quorum.tessera.partyinfo.P2pClient;
import com.quorum.tessera.partyinfo.ResendRequest;
import com.quorum.tessera.api.model.ApiPath;
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
    public boolean sendPartyInfo(String targetUrl, byte[] data) {
       return Objects.nonNull(postDelegate.doPost(targetUrl, ApiPath.PARTYINFO, data));
    }

    @Override
    public boolean makeResendRequest(String targetUrl, ResendRequest request) {
        return postDelegate.makeResendRequest(targetUrl, request);
    }

    
}
