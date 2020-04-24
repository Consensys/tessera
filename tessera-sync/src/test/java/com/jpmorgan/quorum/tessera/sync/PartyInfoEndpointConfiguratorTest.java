package com.jpmorgan.quorum.tessera.sync;

import org.junit.Test;

public class PartyInfoEndpointConfiguratorTest {

    @Test(expected = InstantiationException.class)
    public void getEndpointInstanceInvalidThrowsInstantiationException() throws Exception {
        PartyInfoEndpointConfigurator partyInfoEndpointConfigurator = new PartyInfoEndpointConfigurator();

        partyInfoEndpointConfigurator.getEndpointInstance(BogusEndpoint.class);
    }

    static class BogusEndpoint {}
}
