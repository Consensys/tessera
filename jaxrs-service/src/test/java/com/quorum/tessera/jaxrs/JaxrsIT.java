package com.quorum.tessera.jaxrs;

import com.quorum.tessera.partyinfo.PartyInfoParser;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.api.common.VersionResource;
import com.quorum.tessera.p2p.PartyInfoResource;
import com.quorum.tessera.p2p.TransactionResource;
import com.quorum.tessera.transaction.TransactionManagerImpl;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = JaxrsITConfig.class)
public class JaxrsIT {

    @Inject
    private VersionResource versionResource;

    @Inject
    private PartyInfoResource partyInfoResource;

    @Inject
    private PartyInfoParser partyInfoParser;

    @Inject
    private PartyInfoService partyInfoService;

    @Inject
    private TransactionResource transactionResource;

    @Inject
    private TransactionManagerImpl transactionManager;

    @After
    public void afterTest() {
        verifyNoMoreInteractions(partyInfoParser, partyInfoService, transactionManager);
    }

    @Test
    public void getVersion() throws Exception {
        assertThat(versionResource).isNotNull();
        assertThat(versionResource.getVersion()).isEqualTo("No version defined yet!");
    }

    @Test
    public void partyInfo() {
        byte[] payload = "PAYLOAD".getBytes();

        PartyInfo partyInfo = mock(PartyInfo.class);

        when(partyInfoParser.from(payload)).thenReturn(partyInfo);

        when(partyInfoService.updatePartyInfo(partyInfo)).thenReturn(partyInfo);

        when(partyInfoParser.to(partyInfo)).thenReturn(payload);

        Response response = partyInfoResource.partyInfo(payload);

        assertThat(response.getStatus()).isEqualTo(200);

        verify(partyInfoParser).from(payload);
        verify(partyInfoService).updatePartyInfo(partyInfo);
        verify(partyInfoParser).to(partyInfo);

    }

}
