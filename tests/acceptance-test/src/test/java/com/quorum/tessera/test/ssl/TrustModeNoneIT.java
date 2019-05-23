package com.quorum.tessera.test.ssl;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.server.JerseyServer;
import com.quorum.tessera.test.ssl.sample.SampleApplication;
import config.PortUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import javax.ws.rs.core.Application;

public class TrustModeNoneIT {

    private String serverUri;

    private JerseyServer server;

    private ServerConfig serverConfig;

    @Before
    public void setUp() throws Exception {

        serverUri = "https://localhost:" + new PortUtil(60000).nextPort();

        serverConfig = new ServerConfig();
        serverConfig.setCommunicationType(CommunicationType.REST);
        serverConfig.setServerAddress(serverUri);
        serverConfig.setApp(AppType.P2P);

        serverConfig.setSslConfig(null);

        Application sampleApplication = new SampleApplication();
        server = new JerseyServer(serverConfig, sampleApplication);

        server.start();
    }

    @After
    public void onTearDown() {
        server.stop();
    }

    @Test
    public void allThrough() {

    }
}
