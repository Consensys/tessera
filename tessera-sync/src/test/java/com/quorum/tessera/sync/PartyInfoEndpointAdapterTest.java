package com.quorum.tessera.sync;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class PartyInfoEndpointAdapterTest {

    @Test
    public void create() {
        PartyInfoEndpointAdapter partyInfoEndpointAdapter = new PartyInfoEndpointAdapter();
        assertThat(partyInfoEndpointAdapter.getAppClass())
                .containsExactlyInAnyOrder(PartyInfoEndpoint.class, PartyInfoValidationEndpoint.class);

        assertThat(partyInfoEndpointAdapter.getAppType()).isEqualTo(AppType.P2P);
        assertThat(partyInfoEndpointAdapter.getCommunicationType()).isEqualTo(CommunicationType.WEB_SOCKET);
    }
}
