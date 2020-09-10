package com.quorum.tessera.p2p;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.p2p.recovery.RestResendBatchPublisher;
import com.quorum.tessera.p2p.recovery.RestResendBatchPublisherFactory;
import com.quorum.tessera.recovery.resend.ResendBatchPublisher;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RestResendBatchPublisherFactoryTest {

    private RestResendBatchPublisherFactory resendBatchPublisherFactory;

    @Before
    public void onSetup() {
        resendBatchPublisherFactory = new RestResendBatchPublisherFactory();
        assertThat(resendBatchPublisherFactory.communicationType()).isEqualTo(CommunicationType.REST);
    }

    @Test
    public void create() {
        Config config = mock(Config.class);
        ServerConfig serverConfig = mock(ServerConfig.class);
        when(config.getP2PServerConfig()).thenReturn(serverConfig);

        ResendBatchPublisher result = resendBatchPublisherFactory.create(config);

        assertThat(result).isExactlyInstanceOf(RestResendBatchPublisher.class);
    }
}
