package com.quorum.tessera.client;

import com.quorum.tessera.api.model.ApiPath;
import com.quorum.tessera.api.model.ResendRequest;

import java.net.URI;
import java.util.Objects;

public class RestP2pClient implements P2pClient {
    
    private final PostDelegate postDelegate;

    public RestP2pClient(final PostDelegate postDelegate) {
        this.postDelegate = Objects.requireNonNull(postDelegate);
    }

    @Override
    public byte[] push(final URI target, final byte[] data) {
        return postDelegate.doPost(target, ApiPath.PUSH, data);
    }

    @Override
    public byte[] getPartyInfo(final URI target, final byte[] data) {
       return postDelegate.doPost(target, ApiPath.PARTYINFO, data);
    }

    @Override
    public boolean makeResendRequest(final URI target, final ResendRequest request) {
        return postDelegate.makeResendRequest(target, request);
    }
    
}
