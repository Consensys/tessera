package com.quorum.tessera.sync;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransactionRequesterFactoryTest {

    @Test
    public void newFactory() {
        TransactionRequesterFactory transactionRequesterFactory = TransactionRequesterFactory.newFactory();

        assertThat(transactionRequesterFactory).isNotNull();
    }

    @Test
    public void createTransactionRequester() {
        TransactionRequesterFactory transactionRequesterFactory = TransactionRequesterFactory.newFactory();

        assertThat(transactionRequesterFactory).isNotNull().isExactlyInstanceOf(MockTransactionRequesterFactory.class);
        assertThat(transactionRequesterFactory).isNotNull();

        Config config = mock(Config.class);
        ServerConfig serverConfig = mock(ServerConfig.class);
        when(serverConfig.getCommunicationType()).thenReturn(CommunicationType.REST);
        when(config.getP2PServerConfig()).thenReturn(serverConfig);

        TransactionRequester transactionRequester = transactionRequesterFactory.createTransactionRequester(config);

        assertThat(transactionRequester)
            .isNotNull();


        assertThat(transactionRequester).isNotNull().isExactlyInstanceOf(TransactionRequesterImpl.class);
    }


}
