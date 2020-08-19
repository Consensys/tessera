package com.quorum.tessera.partyinfo;

import com.quorum.tessera.partyinfo.node.Party;

import java.net.URI;
import java.util.stream.Stream;

public interface P2pClient {

    boolean sendPartyInfo(String targetUrl, byte[] data);

    Stream<Party> getParties(URI uri);

}
