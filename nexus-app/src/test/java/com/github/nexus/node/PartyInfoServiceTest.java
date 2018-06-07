package com.github.nexus.node;

import org.junit.Test;

public class PartyInfoServiceTest {
    
    private final PartyInfoService partyInfoService = new PartyInfoServiceImpl();
    
    public PartyInfoServiceTest() {
    }

    @Test(expected = UnsupportedOperationException.class)
    public void initPartyInfo() {
        partyInfoService.initPartyInfo(null, null, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void registerPublicKeys() {
        partyInfoService.registerPublicKeys(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getPartyInfo() {
         partyInfoService.getPartyInfo();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void updatePartyInfo() {
         partyInfoService.updatePartyInfo(null);
    }


}
