package com.quorum.tessera.enclave.websockets;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import java.util.Arrays;
import java.util.NoSuchElementException;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class WebsocketEnclaveClientFactoryTest {

    private WebsocketEnclaveClientFactory websocketEnclaveClientFactory = new WebsocketEnclaveClientFactory();

    @Test
    public void createValid() {

        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setApp(AppType.ENCLAVE);
        serverConfig.setCommunicationType(CommunicationType.WEB_SOCKET);
        serverConfig.setServerAddress("ws:/foo:99");
        
        Config config = new Config();
        config.setServerConfigs(Arrays.asList(serverConfig));

        WebsocketEnclaveClient enclaveClient = websocketEnclaveClientFactory.create(config);
        assertThat(enclaveClient).isNotNull();

    }

    @Test(expected = NoSuchElementException.class)
    public void createWithNoAppType() {

        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setCommunicationType(CommunicationType.WEB_SOCKET);
        serverConfig.setServerAddress("ws:/foo:99");
        
        Config config = new Config();
        config.setServerConfigs(Arrays.asList(serverConfig));

        websocketEnclaveClientFactory.create(config);
    }

    @Test(expected = NoSuchElementException.class)
    public void createWithNoCommunicationType() {

        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setApp(AppType.ENCLAVE);
        serverConfig.setServerAddress("ws:/foo:99");

        Config config = new Config();
        config.setServerConfigs(Arrays.asList(serverConfig));

        websocketEnclaveClientFactory.create(config);
    }
}
