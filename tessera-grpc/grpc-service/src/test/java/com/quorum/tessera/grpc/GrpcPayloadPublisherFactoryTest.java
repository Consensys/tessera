
package com.quorum.tessera.grpc;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.partyinfo.PayloadPublisher;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;


public class GrpcPayloadPublisherFactoryTest {
    
    private GrpcPayloadPublisherFactory payloadPublisherFactory;
    
    @Before
    public void onSetup() {
        payloadPublisherFactory = new GrpcPayloadPublisherFactory();
        assertThat(payloadPublisherFactory.communicationType()).isEqualTo(CommunicationType.GRPC);
    }
    
    @Test
    public void create() {
        Config config = Mockito.mock(Config.class);

        PayloadPublisher publisher = payloadPublisherFactory.create(config);
        
        assertThat(publisher).isExactlyInstanceOf(GrpcPayloadPublisher.class);
    }
    
}
