package com.quorum.tessera.partyinfo;

import com.quorum.tessera.admin.ConfigService;
import com.quorum.tessera.config.FeatureToggles;
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
        final Enclave enclave = mock(Enclave.class);
        final ConfigService configService = mock(ConfigService.class);
        when(configService.getServerUri()).thenReturn(new URI("http://bogus.com"));
        when(configService.featureToggles()).thenReturn(new FeatureToggles());

        final PartyInfoService service = partyInfoServiceFactory.create(enclave, configService);

        assertThat(service).isNotNull();
    }
}
