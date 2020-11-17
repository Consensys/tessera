package com.quorum.tessera.context;

import com.quorum.tessera.config.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class RuntimeContextProviderTest {

    @Before
    @After
    public void clearHolder() {
        RuntimeContextHolder.INSTANCE.setContext(null);
        assertThat(RuntimeContextHolder.INSTANCE.getContext()).isNotPresent();
    }

    @Test
    public void provides() {

        Config confg = mock(Config.class);
        EncryptorConfig encryptorConfig = mock(EncryptorConfig.class);
        when(encryptorConfig.getType()).thenReturn(EncryptorType.NACL);

        when(confg.getEncryptor()).thenReturn(encryptorConfig);

        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);

        when(confg.getKeys()).thenReturn(keyConfiguration);

        ServerConfig serverConfig = mock(ServerConfig.class);
        when(serverConfig.getApp()).thenReturn(AppType.P2P);
        when(serverConfig.getCommunicationType()).thenReturn(CommunicationType.REST);
        when(confg.getP2PServerConfig()).thenReturn(serverConfig);
        when(serverConfig.getServerUri()).thenReturn(URI.create("http://bogus"));
        when(serverConfig.getBindingUri()).thenReturn(URI.create("http://bogus"));
        when(serverConfig.getProperties()).thenReturn(Collections.emptyMap());

        when(confg.getServerConfigs()).thenReturn(List.of(serverConfig));

        FeatureToggles featureToggles = mock(FeatureToggles.class);
        when(confg.getFeatures()).thenReturn(featureToggles);


        try(
            var mockedStaticConfigFactory = mockStatic(ConfigFactory.class);
            var mockStaticRestClientFactory = mockStatic(RestClientFactory.class)
            ) {
            RestClientFactory restClientFactory = mock(RestClientFactory.class);
            when(restClientFactory.buildFrom(serverConfig)).thenReturn(mock(Client.class));
            mockStaticRestClientFactory.when(RestClientFactory::create).thenReturn(restClientFactory);

            ConfigFactory configFactory = mock(ConfigFactory.class);
            when(configFactory.getConfig()).thenReturn(confg);
            mockedStaticConfigFactory.when(ConfigFactory::create).thenReturn(configFactory);

            RuntimeContext runtimeContext = RuntimeContextProvider.provider();
            assertThat(runtimeContext).isNotNull().isSameAs(RuntimeContextProvider.provider());


            mockedStaticConfigFactory.verify(ConfigFactory::create);
            mockedStaticConfigFactory.verifyNoMoreInteractions();

            mockStaticRestClientFactory.verify(RestClientFactory::create);
            mockedStaticConfigFactory.verifyNoMoreInteractions();

        }





    }

}
