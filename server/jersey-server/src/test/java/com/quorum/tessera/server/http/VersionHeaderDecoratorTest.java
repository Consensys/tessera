package com.quorum.tessera.server.http;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.server.jersey.JerseyServer;
import com.quorum.tessera.server.jersey.SampleApplication;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

public class VersionHeaderDecoratorTest {

    private URI serverUri = URI.create("http://localhost:8080");

    private JerseyServer server;

    @Before
    public void onSetUp() throws Exception {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setCommunicationType(CommunicationType.REST);
        serverConfig.setServerAddress("http://localhost:8080");

        Application sample = new SampleApplication();
        server = new JerseyServer(serverConfig, sample);

        server.start();
    }

    @After
    public void onTearDown() throws Exception {
        server.stop();
    }

    @Test
    public void headersPopulatedForJaxrsRequest() {


        Response result = ClientBuilder.newClient().target(serverUri).path("ping").request().get();

        assertThat(result.getStatus()).isEqualTo(200);
        assertThat((String) result.getHeaders().getFirst(VersionHeaderDecorator.API_VERSION_HEADER)).isNotEmpty();

    }


    @Test
    public void headerPopulatedForPlainHttpRequest() throws Exception {

        HttpClient httpClient = HttpClient.newBuilder().build();

        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(serverUri.toString().concat("/ping")))
            .GET()
            .build();

        HttpResponse<String> httpResponse =
            httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        assertThat(httpResponse.statusCode()).isEqualTo(200);
        assertThat(httpResponse.headers().map().get(VersionHeaderDecorator.API_VERSION_HEADER)).isNotEmpty();


    }


}
