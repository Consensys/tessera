package com.quorum.tessera.partyinfo;

import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.model.Recipient;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockPartyInfoStore implements PartyInfoStore {
    @Override
    public PartyInfo getPartyInfo() {
        PartyInfo partyInfo = mock(PartyInfo.class);
        when(partyInfo.getUrl()).thenReturn("http://bogus.com");
        return partyInfo;
    }

    @Override
    public void store(PartyInfo incomingInfo) {

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
    public PartyInfo removeRecipient(String s) {
        return null;
    }
}
