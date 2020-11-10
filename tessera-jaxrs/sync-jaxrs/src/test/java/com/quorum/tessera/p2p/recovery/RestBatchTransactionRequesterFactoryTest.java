package com.quorum.tessera.p2p.recovery;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.recovery.resend.BatchTransactionRequester;
import com.quorum.tessera.recovery.resend.BatchTransactionRequesterFactory;
import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Ignore
public class RestBatchTransactionRequesterFactoryTest {

    @Test
    public void create() {

        ServerConfig p2pServerConfig = mock(ServerConfig.class);
        when(p2pServerConfig.getCommunicationType()).thenReturn(CommunicationType.REST);
        Config config = mock(Config.class);
        when(config.getP2PServerConfig()).thenReturn(p2pServerConfig);

        BatchTransactionRequesterFactory factory = new RestBatchTransactionRequesterFactory();

        BatchTransactionRequester requester = factory.createBatchTransactionRequester(config);

        assertThat(requester).isNotNull();
    }
}
