package com.quorum.tessera.partyinfo;

import com.quorum.tessera.config.*;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PartyInfoServiceFactoryTest {

    private PartyInfoServiceFactory partyInfoServiceFactory;

    @Before
    public void onSetUp() {
        partyInfoServiceFactory = PartyInfoServiceFactory.create();
        assertThat(partyInfoServiceFactory).isExactlyInstanceOf(PartyInfoServiceFactoryImpl.class);
    }

    @Test
    public void createAndLoadStoredInstance() {
        Config config = mock(Config.class);
        when(config.getEncryptor()).thenReturn(EncryptorConfig.getDefault());
        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        when(keyConfiguration.getKeyData()).thenReturn(Collections.emptyList());
        when(config.getKeys()).thenReturn(keyConfiguration);

        ServerConfig serverConfig = mock(ServerConfig.class);
        when(serverConfig.getCommunicationType()).thenReturn(CommunicationType.REST);
        when(serverConfig.getServerUri()).thenReturn(URI.create("http://someplace.com"));
        when(config.getP2PServerConfig()).thenReturn(serverConfig);

        PartyInfoService partyInfoService = partyInfoServiceFactory.create(config);


        assertThat(partyInfoService).isNotNull();


        PartyInfoService anotherPartyInfoService = partyInfoServiceFactory.create(config);

        assertThat(partyInfoService).isSameAs(anotherPartyInfoService);

        assertThat(partyInfoService).isSameAs(anotherPartyInfoService);

        assertThat(partyInfoService).isSameAs(partyInfoServiceFactory.partyInfoService().get());

    }

}
