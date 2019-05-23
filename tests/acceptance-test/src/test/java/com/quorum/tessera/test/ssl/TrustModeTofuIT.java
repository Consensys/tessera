package com.quorum.tessera.test.ssl;

import com.quorum.tessera.config.*;
import com.quorum.tessera.server.JerseyServer;
import com.quorum.tessera.test.ssl.sample.SampleApplication;
import config.PortUtil;
import org.junit.Before;

import javax.ws.rs.core.Application;
import java.nio.file.Paths;

public class TrustModeTofuIT {

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

        serverConfig.setSslConfig(buildTofuSslConfig());

        Application sampleApplication = new SampleApplication();
        server = new JerseyServer(serverConfig, sampleApplication);

        server.start();
    }


    private SslConfig buildTofuSslConfig() {
        SslConfig sslConfig = new SslConfig();
        sslConfig.setTls(SslAuthenticationMode.STRICT);
        sslConfig.setGenerateKeyStoreIfNotExisted(false);

        sslConfig.setSslConfigType(SslConfigType.SERVER_AND_CLIENT);

        sslConfig.setServerTrustMode(SslTrustMode.TOFU);
        sslConfig.setClientTrustMode(SslTrustMode.TOFU);

        sslConfig.setServerKeyStore(Paths.get(getClass().getResource("/certificates/localhost-with-san-keystore.jks").getPath()));
        sslConfig.setServerKeyStorePassword("testtest");

        sslConfig.setClientKeyStore(Paths.get(getClass().getResource("/certificates/quorum-client-keystore.jks").getPath()));
        sslConfig.setClientKeyStorePassword("testtest");

        return sslConfig;
    }
}
