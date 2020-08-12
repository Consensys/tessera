package com.quorum.tessera.partyinfo;

public interface P2pClient {

    boolean sendPartyInfo(String targetUrl, byte[] data);
}
