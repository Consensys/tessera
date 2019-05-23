package com.quorum.tessera.test.ssl;

import com.quorum.tessera.config.*;
import com.quorum.tessera.jaxrs.client.ClientFactory;
import com.quorum.tessera.server.JerseyServer;
import com.quorum.tessera.ssl.context.ClientSSLContextFactoryImpl;
import com.quorum.tessera.ssl.exception.TesseraSecurityException;
import com.quorum.tessera.test.ssl.sample.SampleApplication;
import config.PortUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class TrustModeCaIT {

    private String serverUri;

    private JerseyServer server;

    private ServerConfig serverConfig;

    @Before
    public void setUp() throws Exception {

        serverUri = "https://localhost:" + new PortUtil(12123).nextPort();

        serverConfig = new ServerConfig();
        serverConfig.setCommunicationType(CommunicationType.REST);
        serverConfig.setServerAddress(serverUri);
        serverConfig.setApp(AppType.P2P);

        serverConfig.setSslConfig(buildDefaultSslConfig());

        Application sampleApplication = new SampleApplication();
        server = new JerseyServer(serverConfig, sampleApplication);

        server.start();
    }

    @After
    public void onTearDown() {
        server.stop();
    }

    @Test
    public void testInsecureClientFailsToConnect() {

        assertThatExceptionOfType(ProcessingException.class)
            .isThrownBy(() ->
                ClientBuilder.newClient()
                    .target(serverUri)
                    .path("sample")
                    .request()
                    .get()
            );
    }

    @Test
    public void testSslClientWithValidCertificate() {

        final ClientFactory clientFactory = new ClientFactory(new ClientSSLContextFactoryImpl());
        final Client client = clientFactory.buildFrom(serverConfig);

        Response response = client
            .target(serverUri)
            .path("sample")
            .request()
            .get();

        assertThat(response).isNotNull();
        assertThat(response.readEntity(String.class)).isEqualTo("TEST");

    }

    @Test
    public void testSslClientWithInvalidCertificate() {

        SslConfig newSslConfig = buildDefaultSslConfig();
        newSslConfig.setClientKeyStore(Paths.get(getClass().getResource("/certificates/localhost-with-san-keystore.jks").getPath()));
        newSslConfig.setClientKeyStorePassword("testtest");
        newSslConfig.setClientTrustStore(Paths.get(getClass().getResource("/certificates/truststore.jks").getPath()));
        newSslConfig.setClientTrustStorePassword("testtest");

        ServerConfig newConfig = serverConfig;
        newConfig.setSslConfig(newSslConfig);

        final ClientFactory clientFactory = new ClientFactory(new ClientSSLContextFactoryImpl());
        final Client client = clientFactory.buildFrom(newConfig);

        assertThatExceptionOfType(ProcessingException.class)
            .isThrownBy(() ->
                client
                    .target(serverUri)
                    .path("test")
                    .request()
                    .get());
    }

    @Test
    public void testSslClientWithCertificateNotTrusted() {
        SslConfig newSslConfig = buildDefaultSslConfig();
        newSslConfig.setClientTrustStore(Paths.get(getClass().getResource("/certificates/quorum-client-keystore.jks").getPath()));

        ServerConfig newConfig = serverConfig;
        newConfig.setSslConfig(newSslConfig);

        final ClientFactory clientFactory = new ClientFactory(new ClientSSLContextFactoryImpl());
        final Client client = clientFactory.buildFrom(newConfig);

        assertThatExceptionOfType(ProcessingException.class)
            .isThrownBy(() ->
                client
                    .target(serverUri)
                    .path("sample")
                    .request()
                    .get());

    }

    @Test
    public void testSslClientWrongKeystorePassword() {
        SslConfig newSslConfig = buildDefaultSslConfig();
        newSslConfig.setClientKeyStorePassword("bogus");

        ServerConfig newConfig = serverConfig;
        newConfig.setSslConfig(newSslConfig);

        final ClientFactory clientFactory = new ClientFactory(new ClientSSLContextFactoryImpl());

        assertThatExceptionOfType(TesseraSecurityException.class)
            .isThrownBy(() -> clientFactory.buildFrom(newConfig));

    }


    private SslConfig buildDefaultSslConfig() {
        SslConfig sslConfig = new SslConfig();
        sslConfig.setTls(SslAuthenticationMode.STRICT);
        sslConfig.setGenerateKeyStoreIfNotExisted(false);

        sslConfig.setSslConfigType(SslConfigType.SERVER_AND_CLIENT);

        sslConfig.setServerTrustMode(SslTrustMode.CA);
        sslConfig.setClientTrustMode(SslTrustMode.CA);

        sslConfig.setServerKeyStore(Paths.get(getClass().getResource("/certificates/localhost-with-san-keystore.jks").getPath()));
        sslConfig.setServerKeyStorePassword("testtest");
        sslConfig.setServerTrustStore(Paths.get(getClass().getResource("/certificates/truststore.jks").getPath()));
        sslConfig.setServerTrustStorePassword("testtest");

        sslConfig.setClientKeyStore(Paths.get(getClass().getResource("/certificates/quorum-client-keystore.jks").getPath()));
        sslConfig.setClientKeyStorePassword("testtest");
        sslConfig.setClientTrustStore(Paths.get(getClass().getResource("/certificates/truststore.jks").getPath()));
        sslConfig.setClientTrustStorePassword("testtest");

        return sslConfig;
    }
}
