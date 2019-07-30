package com.quorum.tessera.partyinfo;

import com.jpmorgan.quorum.mock.servicelocator.MockServiceLocator;
import com.quorum.tessera.admin.ConfigService;
import com.quorum.tessera.enclave.Enclave;
import java.net.URI;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.*;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class PartyInfoServiceFactoryTest {

    private PartyInfoServiceFactory partyInfoServiceFactory;

    @Before
    public void onSetUp() {
        partyInfoServiceFactory = PartyInfoServiceFactory.create();
    }

    @Test
    public void loadServicesFromLocator() throws Exception {

        ResendManager resendManager = mock(ResendManager.class);

        PayloadPublisher payloadPublisher = mock(PayloadPublisher.class);
        ConfigService configService = mock(ConfigService.class);
        when(configService.getServerUri()).thenReturn(new URI("http://bogus.com"));
        Enclave enclave = mock(Enclave.class);

        PartyInfoService partyInfoService = mock(PartyInfoService.class);

        Set services =
                Stream.of(payloadPublisher, configService, enclave, partyInfoService, resendManager)
                        .collect(Collectors.toSet());

        MockServiceLocator mockServiceLocator = MockServiceLocator.createMockServiceLocator();

        mockServiceLocator.setServices(services);

        assertThat(partyInfoServiceFactory.partyInfoService()).isSameAs(partyInfoService);
        assertThat(partyInfoServiceFactory.resendManager()).isSameAs(resendManager);
    }

    @Test(expected = IllegalStateException.class)
    public void loadServicesFromLocatorServiceNotFoundThrowsIllegalStateException() throws Exception {

        MockServiceLocator mockServiceLocator = MockServiceLocator.createMockServiceLocator();

        mockServiceLocator.setServices(Collections.emptySet());

        partyInfoServiceFactory.partyInfoService();
    }
}
