package com.github.nexus.api;

import com.github.nexus.node.PartyInfo;
import com.github.nexus.node.PartyInfoParser;
import com.github.nexus.node.PartyInfoService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.Response;
import java.io.IOException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class PartyInfoResourceTest {

    @Mock
    private PartyInfoService partyInfoService;

    private PartyInfoResource partyInfoResource;

    @Mock
    private PartyInfoParser partyInfoParser;

    @Before
    public void onSetup() {
        MockitoAnnotations.initMocks(this);
        partyInfoResource = new PartyInfoResource(partyInfoService, partyInfoParser);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(partyInfoService,partyInfoParser);
    }

    @Test
    public void testPartyInfo() throws IOException {
        
        byte[] data = "{}".getBytes();
        
        PartyInfo partyInfo = mock(PartyInfo.class);
        when(partyInfoParser.from(data)).thenReturn(partyInfo);
        when(partyInfoService.updatePartyInfo(partyInfo)).thenReturn(partyInfo);
        
        byte[] resultData = "I LOVE SPARROWS!!".getBytes();
        
        when(partyInfoParser.to(partyInfo)).thenReturn(resultData);
        
        Response response = partyInfoResource.partyInfo(data);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(201);
        assertThat(response.getEntity())
                .isEqualTo(Entity.entity(resultData, MediaType.APPLICATION_OCTET_STREAM));
        
        verify(partyInfoParser).from(data);
        verify(partyInfoService).updatePartyInfo(partyInfo);
        verify(partyInfoParser).to(partyInfo);

    }

}
