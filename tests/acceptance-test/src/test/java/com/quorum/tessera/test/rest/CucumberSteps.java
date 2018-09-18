package com.quorum.tessera.test.rest;

import cucumber.api.java8.En;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import static org.assertj.core.api.Assertions.assertThat;

public class CucumberSteps implements En {

    private static final URI SERVER_URI = UriBuilder.fromUri("http://127.0.0.1").port(8080).build();

    public CucumberSteps() {
        Given("that the server is running", () -> {
        });

        List<Response> responses = new ArrayList<>();

        When("client makes (.*) request", (path) -> {

            Response response = ClientBuilder.newClient().target(SERVER_URI).path("/" + path)
                    .request().buildGet().invoke();

            responses.add(response);
        });

        Then("the server responds with status (\\d+) and body (.*) and contentType (.*)", (status, body, contentType) -> {

            assertThat(responses).hasSize(1);
            Response response = responses.get(0);
            assertThat(response.getStatus()).isEqualTo(status);
            MediaType mediaType = response.getMediaType();

            assertThat(mediaType.toString()).isEqualTo(contentType);
            if (mediaType == MediaType.TEXT_PLAIN_TYPE) {
                assertThat(response.readEntity(String.class)).isEqualTo(body);
            }

        });
    }

}
