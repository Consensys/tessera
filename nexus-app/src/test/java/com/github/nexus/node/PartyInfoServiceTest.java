package com.github.nexus.node;

import com.github.nexus.enclave.keys.model.Key;
import org.assertj.core.util.Arrays;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;

public class PartyInfoServiceTest {
    
    private final PartyInfoService partyInfoService = new PartyInfoServiceImpl();
    
    public PartyInfoServiceTest() {
    }

    private String url = "http://someurl.com";

    @Test
    public void testInitPartyInfo() {
        partyInfoService.initPartyInfo(url, new String[]{"node1","node2"});
        assertEquals(2,partyInfoService.getPartyInfo().getParties().size());
        assertEquals(url, partyInfoService.getPartyInfo().getUrl());
    }

    @Test
    public void testRegisterPublicKeys() {
        Key key = new Key("somekey".getBytes());
        partyInfoService.initPartyInfo(url, new String[]{});
        partyInfoService.registerPublicKeys(Arrays.array(key));
        assertEquals(1, partyInfoService.getPartyInfo().getRecipients().size());
        assertThat(partyInfoService.getPartyInfo().getRecipients().get(0).getKey()).isSameAs(key);
    }

    @Test
    public void testUpdatePartyInfo() {

    }


}
