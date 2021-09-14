package com.quorum.tessera.server.jaxrs;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.server.jersey.JerseyServer;
import com.quorum.tessera.server.jersey.SampleApplication;
import com.quorum.tessera.ssl.context.ClientSSLContextFactoryImpl;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.nio.file.Path;
import javax.net.ssl.SSLContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ServerMutualSslIT {

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
  public void pingWithCerts() {

    SslConfig clientSslConfig = new SslConfig();
    clientSslConfig.setTls(SslAuthenticationMode.STRICT);
    clientSslConfig.setSslConfigType(SslConfigType.CLIENT_ONLY);
    clientSslConfig.setClientKeyStore(
        Path.of(getClass().getResource("/certificates/client.jks").getPath()));
    clientSslConfig.setClientKeyStorePassword("testtest".toCharArray());
    clientSslConfig.setClientTrustMode(SslTrustMode.CA);
    clientSslConfig.setClientTrustStore(
        Path.of(getClass().getResource("/certificates/truststore.jks").getPath()));
    clientSslConfig.setClientTrustStorePassword("testtest".toCharArray());

    final SSLContext sslContext =
        new ClientSSLContextFactoryImpl().from(serverUri.toString(), clientSslConfig);

    Response result =
        ClientBuilder.newBuilder()
            .sslContext(sslContext)
            .build()
            .target(serverUri)
            .path("ping")
            .request()
            .get();

    assertThat(result.getStatus()).isEqualTo(200);
    assertThat(result.readEntity(String.class)).isEqualTo("HEllow");
  }
}
