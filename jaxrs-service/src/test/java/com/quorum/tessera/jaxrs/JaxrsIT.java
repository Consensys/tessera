package com.quorum.tessera.jaxrs;

import com.quorum.tessera.api.PartyInfoResource;
import com.quorum.tessera.api.TransactionResource;
import com.quorum.tessera.api.VersionResource;
import com.quorum.tessera.enclave.EnclaveMediator;
import com.quorum.tessera.node.PartyInfoParser;
import com.quorum.tessera.node.PartyInfoService;
import com.quorum.tessera.node.model.PartyInfo;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

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
    private EnclaveMediator enclaveMediator;

    @After
    public void afterTest() {
        verifyNoMoreInteractions(partyInfoParser, partyInfoService, enclaveMediator);
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

    @Test
    public void deleteKey() {
        String key = "SomeKey";
        Response response = transactionResource.deleteKey(key);
        assertThat(response.getStatus()).isEqualTo(204);

        verify(enclaveMediator).deleteKey(key);

        verifyNoMoreInteractions(enclaveMediator);
    }

}
