
package com.quorum.tessera.p2p;

import com.quorum.tessera.config.CommunicationType;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;


public class RestPartyInfoPollerFactoryTest {
    
    @Test
    public void newInstanceHasCorrectCommuicationType() {
        assertThat(new RestPartyInfoPollerFactory().communicationType()).isEqualTo(CommunicationType.REST);
    }
    
}
