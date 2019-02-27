package com.quorum.tessera.enclave.websockets;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.config.cli.CliDelegate;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.server.TesseraServer;
import com.quorum.tessera.server.TesseraServerFactory;
import com.quorum.tessera.service.Service;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.net.URL;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@Configuration
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = EnclaveWebsocketIT.class)
@ImportResource(locations = "classpath:/tessera-enclave-websocket-spring.xml")
public class EnclaveWebsocketIT {

    private TesseraServerFactory serverFactory = TesseraServerFactory.create(CommunicationType.WEB_SOCKET);
    
    private TesseraServer server;

    @BeforeClass
    public static void onClass() throws Exception {
        URL url = EnclaveWebsocketIT.class.getResource("/sample-config.json");
        CliDelegate.INSTANCE.execute("-configfile", url.getFile());
    }


    @Inject
    private Config config;

    private WebsocketEnclaveClient enclaveClient;

    @Before
    public void setUp() throws Exception {

        ServerConfig serverConfig = config.getServerConfigs().stream()
                .filter(s -> s.getApp() == AppType.ENCLAVE)
                .filter(s -> s.getCommunicationType() == CommunicationType.WEB_SOCKET)
                .findAny().get();

        server = serverFactory.createServer(serverConfig, Collections.singleton(EnclaveEndpoint.class));
        server.start();
        
        
        WebsocketEnclaveClientFactory enclaveClientFactory = new WebsocketEnclaveClientFactory();

        enclaveClient = enclaveClientFactory.create(config);
        enclaveClient.start();
    }

    @After
    public void tearDown() throws Exception {
        enclaveClient.stop();
        server.stop();
    }

    @Test
    public void defaultPublicKey() {
        PublicKey result = enclaveClient.defaultPublicKey();

        assertThat(result).isNotNull();
        assertThat(result.encodeToBase64()).isEqualTo("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");
    }

    @Test
    public void forwardingKeys() {

        Set<PublicKey> result = enclaveClient.getForwardingKeys();

        assertThat(result).isEmpty();
    }

    @Test
    public void getPublicKeys() {

        Set<PublicKey> result = enclaveClient.getPublicKeys();

        assertThat(result).hasSize(1);
        assertThat(result.iterator().next().encodeToBase64()).isEqualTo("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");
    }

    @Test
    public void status() {
        assertThat(enclaveClient.status())
                .isEqualTo(Service.Status.STARTED);

    }

}
