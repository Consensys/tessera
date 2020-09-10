package com.quorum.tessera.p2p.resend;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class TransactionRequesterFactoryTest {

    private TransactionRequesterFactory transactionRequesterFactory;

    @Before
    public void onSetUp() {
        transactionRequesterFactory = TransactionRequesterFactory.newFactory();
        assertThat(transactionRequesterFactory).isNotNull().isExactlyInstanceOf(MockTransactionRequesterFactory.class);
    }

    @Test
    public void createTransactionRequester() {

        Config config = mock(Config.class);
        ServerConfig serverConfig = mock(ServerConfig.class);
        when(serverConfig.getCommunicationType()).thenReturn(CommunicationType.REST);
        when(config.getP2PServerConfig()).thenReturn(serverConfig);

        TransactionRequester transactionRequester = transactionRequesterFactory.createTransactionRequester(config);

        assertThat(transactionRequester).isNotNull();
        verifyZeroInteractions(transactionRequester);
    }


}
