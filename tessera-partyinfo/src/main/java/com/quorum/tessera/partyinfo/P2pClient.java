package com.quorum.tessera.partyinfo;

public interface P2pClient {

    byte[] push(String targetUrl, byte[] data);

    boolean sendPartyInfo(String targetUrl, byte[] data);
}
