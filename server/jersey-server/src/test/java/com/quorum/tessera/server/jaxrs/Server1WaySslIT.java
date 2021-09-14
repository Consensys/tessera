package com.quorum.tessera.server.jaxrs;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.server.jersey.JerseyServer;
import com.quorum.tessera.server.jersey.SampleApplication;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class Server1WaySslIT {

  private final URI serverUri = URI.create("https://localhost:8080");

  private JerseyServer server;

  @Before
  public void onSetUp() throws Exception {

    ServerConfig serverConfig = new ServerConfig();
    serverConfig.setCommunicationType(CommunicationType.REST);
    serverConfig.setServerAddress("https://localhost:8080");

    SslConfig sslConfig = new SslConfig();
    sslConfig.setTls(SslAuthenticationMode.STRICT);
    sslConfig.setSslConfigType(SslConfigType.SERVER_ONLY);
    sslConfig.setServerKeyStore(
        Path.of(getClass().getResource("/certificates/server-localhost-with-san.jks").getPath()));
    sslConfig.setServerKeyStorePassword("testtest".toCharArray());
    sslConfig.setServerTrustMode(SslTrustMode.CA);
    sslConfig.setServerTrustStore(
        Path.of(getClass().getResource("/certificates/truststore.jks").getPath()));
    sslConfig.setServerTrustStorePassword("testtest".toCharArray());
    sslConfig.setClientAuth(false);

    serverConfig.setSslConfig(sslConfig);

    JaxbUtil.marshalWithNoValidation(serverConfig, System.out);

    server = new JerseyServer(serverConfig, SampleApplication.class);

    server.start();
  }

  @After
  public void onTearDown() {
    server.stop();
  }

  @Test(expected = ProcessingException.class)
  public void pingWillFail() {

    Response result = ClientBuilder.newClient().target(serverUri).path("ping").request().get();

    assertThat(result.getStatus()).isEqualTo(200);
    assertThat(result.readEntity(String.class)).isEqualTo("HEllow");
  }

  @Test
  public void clientOnlyNeedToTrustServer()
      throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {

    final Path trustStorePath =
        Path.of(getClass().getResource("/certificates/truststore.jks").getPath());
    final KeyStore trustStore =
        KeyStore.getInstance(trustStorePath.toFile(), "testtest".toCharArray());

    final Client client = ClientBuilder.newBuilder().trustStore(trustStore).build();

    final Response result = client.target(serverUri).path("ping").request().get();

    assertThat(result.getStatus()).isEqualTo(200);
    assertThat(result.readEntity(String.class)).isEqualTo("HEllow");
  }
}
