
package com.jpmorgan.quorum.tessera.sync;

import com.quorum.tessera.partyinfo.PartyInfoService;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import static org.mockito.Mockito.mock;


public class PartyInfoEndpointConfiguratorTest {
    

    @Test
    public void getEndpointInstance() throws InstantiationException {
        PartyInfoEndpointConfigurator partyInfoEndpointConfigurator = new PartyInfoEndpointConfigurator();
        
        PartyInfoService partyInfoService = mock(PartyInfoService.class);
        new PartyInfoServiceHolder(partyInfoService);
        
        PartyInfoEndpoint result = partyInfoEndpointConfigurator.getEndpointInstance(PartyInfoEndpoint.class);
        
        assertThat(result).isNotNull();
    }
    
    @Test(expected = InstantiationException.class)
    public void getEndpointInstanceForUnsupportedType() throws InstantiationException {
        PartyInfoEndpointConfigurator partyInfoEndpointConfigurator = new PartyInfoEndpointConfigurator();
        partyInfoEndpointConfigurator.getEndpointInstance(BogusEndpoint.class);
    }
    
    static class BogusEndpoint {}
    
}
