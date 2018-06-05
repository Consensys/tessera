package com.github.nexus.api;

import com.github.nexus.service.PartyInfoService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verifyNoMoreInteractions;

public class PartyInfoResourceTest {

    @Mock
    private PartyInfoService partyInfoService;

    private PartyInfoResource partyInfoResource;

    @Before
    public void onSetup() {
        MockitoAnnotations.initMocks(this);
        partyInfoResource = new PartyInfoResource(partyInfoService);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(partyInfoService);
    }

    @Test
    public void testPartyInfo(){

    }

}
