package com.quorum.tessera.p2p;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.partyinfo.TransactionRequester;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransactionRequesterFactoryImplTest {

    @Test
    public void createTransactionRequester() {
        TransactionRequesterFactoryImpl factory = new TransactionRequesterFactoryImpl();

        Config config = mock(Config.class);
        ServerConfig serverConfig = mock(ServerConfig.class);
        when(serverConfig.getCommunicationType()).thenReturn(CommunicationType.REST);
        when(config.getP2PServerConfig()).thenReturn(serverConfig);

        TransactionRequester requester = factory.createTransactionRequester(config);

        assertThat(requester).isNotNull().isExactlyInstanceOf(TransactionRequesterImpl.class);

    }



}
