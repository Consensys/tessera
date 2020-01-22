package com.quorum.tessera.sync;

import com.quorum.tessera.partyinfo.ResendRequest;

public interface ResendClient {

    boolean makeResendRequest(String targetUrl, ResendRequest request);
}
