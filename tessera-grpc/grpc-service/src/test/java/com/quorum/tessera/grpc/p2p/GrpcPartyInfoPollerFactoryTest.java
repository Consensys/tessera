
package com.quorum.tessera.grpc.p2p;

import com.quorum.tessera.config.CommunicationType;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;


public class GrpcPartyInfoPollerFactoryTest {
    
    @Test
    public void newInstanceHasCorrectCommuicationType() {
        assertThat(new GrpcPartyInfoPollerFactory().communicationType()).isEqualTo(CommunicationType.GRPC);
    }
    
}
