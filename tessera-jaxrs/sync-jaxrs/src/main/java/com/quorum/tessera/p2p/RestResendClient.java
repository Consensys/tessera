package com.quorum.tessera.p2p;

import com.quorum.tessera.partyinfo.ResendRequest;
import com.quorum.tessera.sync.ResendClient;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Objects;

public class RestResendClient implements ResendClient {

    private final Client client;

    public RestResendClient(Client client) {
        this.client = Objects.requireNonNull(client);
    }

    @Override
    public boolean makeResendRequest(String targetUrl, ResendRequest request) {
        try (Response response =
                client.target(targetUrl)
                        .path("/resend")
                        .request()
                        .post(Entity.entity(request, MediaType.APPLICATION_JSON))) {

            return Response.Status.OK.getStatusCode() == response.getStatus();
        }
    }
}
