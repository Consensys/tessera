package com.quorum.tessera.partyinfo;

import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.partyinfo.node.Party;
import com.quorum.tessera.partyinfo.node.Recipient;
import com.quorum.tessera.partyinfo.node.VersionInfo;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockPartyInfoStore implements PartyInfoStore {
    @Override
    public NodeInfo getPartyInfo() {
        NodeInfo partyInfo = mock(NodeInfo.class);
        when(partyInfo.getUrl()).thenReturn("http://bogus.com");
        return partyInfo;
    }

    @Override
    public void store(NodeInfo incomingInfo) {

    }

    @Override
    public Recipient findRecipientByPublicKey(PublicKey from) {
        return null;
    }

    @Override
    public String getAdvertisedUrl() {
        return null;
    }

    @Override
    public NodeInfo removeRecipient(String s) {
        return null;
    }

    @Override
    public VersionInfo getVersionInfo(Party party) {
        return null;
    }
}
