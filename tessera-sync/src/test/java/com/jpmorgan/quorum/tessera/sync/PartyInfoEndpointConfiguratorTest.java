package com.jpmorgan.quorum.tessera.sync;

import com.jpmorgan.quorum.mock.servicelocator.MockServiceLocator;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.transaction.EncryptedTransactionDAO;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;
import org.mockito.Mockito;

public class PartyInfoEndpointConfiguratorTest {

    @Test
    public void getEndpointInstance() throws Exception {

        MockServiceLocator mockServiceLocator = MockServiceLocator.createMockServiceLocator();

        Set services = Stream.of(Enclave.class, EncryptedTransactionDAO.class, PartyInfoService.class)
                .map(Mockito::mock).collect(Collectors.toSet());

        mockServiceLocator.setServices(services);

        PartyInfoEndpointConfigurator partyInfoEndpointConfigurator = new PartyInfoEndpointConfigurator();

        PartyInfoEndpoint endpoint = partyInfoEndpointConfigurator.getEndpointInstance(PartyInfoEndpoint.class);

        assertThat(endpoint).isNotNull();

        services.forEach(Mockito::verifyZeroInteractions);

    }

    @Test(expected = InstantiationException.class)
    public void getEndpointInstanceInvalidThrowsInstantiationException() throws Exception {
        PartyInfoEndpointConfigurator partyInfoEndpointConfigurator = new PartyInfoEndpointConfigurator();

        partyInfoEndpointConfigurator.getEndpointInstance(BogusEndpoint.class);
    }

    static class BogusEndpoint {
    }
}
