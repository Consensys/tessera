package com.quorum.tessera.grpc.p2p;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.partyinfo.ResendBatchPublisher;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;

public class GrpcResendBatchPublisherFactoryTest {

    private GrpcResendBatchPublisherFactory grpcResendBatchPublisherFactory;

    @Before
    public void onSetUp() {
        grpcResendBatchPublisherFactory = new GrpcResendBatchPublisherFactory();
        assertThat(grpcResendBatchPublisherFactory.communicationType()).isEqualTo(CommunicationType.GRPC);

    }

    @Test
    public void createWithMockConfig() {
        Config config = mock(Config.class);
        ResendBatchPublisher result = grpcResendBatchPublisherFactory.create(config);
        assertThat(result).isExactlyInstanceOf(GrpcResendBatchPublisher.class);
    }

}
