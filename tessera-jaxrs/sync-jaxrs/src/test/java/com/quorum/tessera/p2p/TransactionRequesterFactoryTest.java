package com.quorum.tessera.p2p;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.sync.TransactionRequester;
import com.quorum.tessera.sync.TransactionRequesterFactory;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransactionRequesterFactoryTest {

    private TransactionRequesterFactory transactionRequesterFactory;

    @Before
    public void onSetUp() {
        transactionRequesterFactory = new TransactionRequesterFactoryImpl();
    }


    @Test
    public void createInstance() {
        Config config = mock(Config.class);
        ServerConfig serverConfig = mock(ServerConfig.class);
        when(serverConfig.getCommunicationType()).thenReturn(CommunicationType.REST);
        when(config.getP2PServerConfig()).thenReturn(serverConfig);
        TransactionRequester transactionRequester = transactionRequesterFactory.createTransactionRequester(config);
        assertThat(transactionRequester).isNotNull();
    }

}
