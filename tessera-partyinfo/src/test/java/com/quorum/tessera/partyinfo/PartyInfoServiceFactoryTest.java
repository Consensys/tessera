package com.quorum.tessera.partyinfo;

import com.quorum.tessera.admin.ConfigService;
import com.quorum.tessera.enclave.Enclave;
import java.net.URI;
import static org.assertj.core.api.Assertions.*;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class PartyInfoServiceFactoryTest {

    private PartyInfoServiceFactory partyInfoServiceFactory;

    @Before
    public void onSetUp() {
        partyInfoServiceFactory = PartyInfoServiceFactory.newFactory();
    }

    @Test
    public void create() throws Exception {
        ConfigService configService = mock(ConfigService.class);
        when(configService.getServerUri()).thenReturn(new URI("http://bogus.com"));
        Enclave enclave = mock(Enclave.class);
        PartyInfoService service = partyInfoServiceFactory.create(enclave, configService);

        assertThat(service).isNotNull();
    }
}
