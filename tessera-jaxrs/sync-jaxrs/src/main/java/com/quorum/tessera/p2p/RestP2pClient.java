package com.quorum.tessera.p2p;

import com.quorum.tessera.partyinfo.P2pClient;
import com.quorum.tessera.partyinfo.ResendRequest;
import java.util.Objects;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class RestP2pClient implements P2pClient {

    private final Client client;

    public RestP2pClient(Client client) {
        this.client = Objects.requireNonNull(client);
    }

    @Override
    public byte[] push(String targetUrl, byte[] data) {

        try (Response response =
                client.target(targetUrl)
                        .path("/push")
                        .request()
                        .post(Entity.entity(data, MediaType.APPLICATION_OCTET_STREAM_TYPE))) {

            if (Response.Status.OK.getStatusCode() != response.getStatus()
                    && Response.Status.CREATED.getStatusCode() != response.getStatus()) {
                return null;
            }

            return response.readEntity(byte[].class);
        }
    }

    @Override
    public boolean sendPartyInfo(String targetUrl, byte[] data) {
        try (Response response =
                client.target(targetUrl)
                        .path("/partyinfo")
                        .request()
                        .post(Entity.entity(data, MediaType.APPLICATION_OCTET_STREAM_TYPE))) {

            if (Response.Status.OK.getStatusCode() != response.getStatus()
                    && Response.Status.CREATED.getStatusCode() != response.getStatus()) {
                return false;
            }

            return Objects.nonNull(response.readEntity(byte[].class));
        }
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
