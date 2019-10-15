package com.quorum.tessera.partyinfo;

public interface P2pClient<T> {

    byte[] push(String targetUrl, byte[] data);

    boolean sendPartyInfo(String targetUrl, byte[] data);

    boolean makeResendRequest(String targetUrl, T request);
}
