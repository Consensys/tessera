package com.quorum.tessera.partyinfo;

import com.jpmorgan.quorum.mock.servicelocator.MockServiceLocator;
import com.quorum.tessera.enclave.Enclave;
import org.junit.Test;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class PartyInfoServiceFactoryTest {

    @Test
    public void loadServicesFromLocator() throws Exception {

        //        ResendManager resendManager = mock(ResendManager.class);
        PayloadPublisher payloadPublisher = mock(PayloadPublisher.class);

        Enclave enclave = mock(Enclave.class);
        PartyInfoService partyInfoService = mock(PartyInfoService.class);
        PartyInfoStore store = mock(PartyInfoStore.class);

        Set<Object> services =
                Stream.of(payloadPublisher, enclave, partyInfoService, store).collect(Collectors.toSet());

        final MockServiceLocator mockServiceLocator = MockServiceLocator.createMockServiceLocator();
        mockServiceLocator.setServices(services);

        final PartyInfoServiceFactory partyInfoServiceFactory = PartyInfoServiceFactory.create();

        assertThat(partyInfoServiceFactory.partyInfoService()).isSameAs(partyInfoService);
        assertThat(partyInfoServiceFactory.enclave()).isSameAs(enclave);
        assertThat(partyInfoServiceFactory.payloadPublisher()).isSameAs(payloadPublisher);
        //        assertThat(partyInfoServiceFactory.resendManager()).isSameAs(resendManager);
        assertThat(partyInfoServiceFactory.partyInfoStore()).isSameAs(store);
    }

    @Test(expected = IllegalStateException.class)
    public void loadServicesFromLocatorServiceNotFoundThrowsIllegalStateException() {
        MockServiceLocator.createMockServiceLocator().setServices(Collections.emptySet());

        final PartyInfoServiceFactory partyInfoServiceFactory = PartyInfoServiceFactory.create();

        partyInfoServiceFactory.partyInfoService();
    }
}
