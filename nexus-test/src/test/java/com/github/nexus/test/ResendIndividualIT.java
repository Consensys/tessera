package com.github.nexus.test;

import com.github.nexus.api.model.ResendRequest;
import com.github.nexus.api.model.ResendRequestType;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class ResendIndividualIT {

    private static final URI PRIMARY_SERVER_URI = UriBuilder.fromUri("http://127.0.0.1").port(8080).build();

    private static final URI SECONDARY_SERVER_URI = UriBuilder.fromUri("http://127.0.0.1").port(8081).build();

    private static final Client client = ClientBuilder.newClient();

    private static final String RESEND_PATH = "/resend";

    private static final String RECIPIENT_KEY = "yGcjkFyZklTTXrn8+WIkYwicA2EGBn9wZFkctAad4X0=";

    @Test
    public void resendTransactionsForGivenKey() {
    }

}
