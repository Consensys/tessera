package com.github.nexus.node;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Objects;

public class PartyInfoPostDelegate {

    private static final String PATH = "/partyinfo";

    private final Client client;

    public PartyInfoPostDelegate(final Client client) {
        this.client = Objects.requireNonNull(client);
    }

    public byte[] doPost(final String url, final byte[] data) {
        final Response response = client
            .target(url)
            .path(PATH)
            .request()
            .post(Entity.entity(data,MediaType.APPLICATION_OCTET_STREAM_TYPE));

        return response.readEntity(byte[].class);
    }

}
