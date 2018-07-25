package com.quorum.tessera.api;

import com.quorum.tessera.node.PartyInfoParser;
import com.quorum.tessera.node.PartyInfoService;
import com.quorum.tessera.node.model.PartyInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import java.io.IOException;
import java.io.OutputStream;

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
        assertThat(response.getStatus()).isEqualTo(200);

        StreamingOutput o = (StreamingOutput) response.getEntity();
        o.write(mock(OutputStream.class));

        assertThat(o).isNotNull();


        verify(partyInfoParser).from(data);
        verify(partyInfoService).updatePartyInfo(partyInfo);
        verify(partyInfoParser).to(partyInfo);

    }

}
