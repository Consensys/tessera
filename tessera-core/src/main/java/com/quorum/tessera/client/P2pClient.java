package com.quorum.tessera.client;

import com.quorum.tessera.api.model.ResendRequest;

import java.net.URI;

public interface P2pClient {

    byte[] push(URI targetUrl, byte[] data);

    byte[] getPartyInfo(URI targetUrl, byte[] data);

    boolean makeResendRequest(URI targetUrl, ResendRequest request);
 
}
