package com.jpmorgan.quorum.tessera.sync;

import com.quorum.tessera.partyinfo.PartyInfoService;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class PartyInfoServiceHolderTest {

    @Test
    public void createAndGet() {
        PartyInfoService partyInfoService = mock(PartyInfoService.class);
        PartyInfoServiceHolder instance = new PartyInfoServiceHolder(partyInfoService);

        assertThat(instance).isNotNull();
        assertThat(PartyInfoServiceHolder.getPartyInfoService()).isSameAs(partyInfoService);
    }

    @Test(expected = IllegalStateException.class)
    public void getWithoutConstruct() {
        PartyInfoServiceHolder.getPartyInfoService();
    }
}
