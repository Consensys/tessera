package com.quorum.tessera.server.http;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.server.utils.ServerUtils;
import com.quorum.tessera.shared.Constants;
import jakarta.servlet.DispatcherType;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.EnumSet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class VersionHeaderDecoratorTest {

  private URI serverUri = URI.create("http://localhost:8080");

  private Server server;

  @Before
  public void onSetUp() throws Exception {
    System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

    ServerConfig serverConfig = new ServerConfig();
    serverConfig.setCommunicationType(CommunicationType.REST);
    serverConfig.setServerAddress("http://localhost:8080");

    final ResourceConfig config = new ResourceConfig(SomeResource.class);

    this.server = ServerUtils.buildWebServer(serverConfig);

    ServletContextHandler context = new ServletContextHandler(server, "/");
    ServletContainer servletContainer = new ServletContainer(config);
    ServletHolder jerseyServlet = new ServletHolder(servletContainer);

    context.addServlet(jerseyServlet, "/*");

    // Sample Usage
    context.addFilter(VersionHeaderDecorator.class, "/*", EnumSet.allOf(DispatcherType.class));

    server.start();
  }

  @After
  public void onTearDown() throws Exception {
    server.stop();
  }

  //    @Test
  //    public void headersPopulatedForJaxrsRequest() {
  //
  //        Response result =
  // ClientBuilder.newClient().target(serverUri).path("ping").request().get();
  //
  //        assertThat(result.getStatus()).isEqualTo(200);
  //        assertThat((String)
  // result.getHeaders().getFirst(Constants.API_VERSION_HEADER)).isNotEmpty();
  //    }

  @Test
  public void headerPopulatedForPlainHttpRequest() throws Exception {

    HttpClient httpClient = HttpClient.newBuilder().build();

    HttpRequest httpRequest =
        HttpRequest.newBuilder()
            .uri(URI.create(serverUri.toString().concat("/ping")))
            .GET()
            .build();

    HttpResponse<String> httpResponse =
        httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

    assertThat(httpResponse.statusCode()).isEqualTo(200);
    assertThat(httpResponse.headers().map().get(Constants.API_VERSION_HEADER)).isNotEmpty();
  }
}
