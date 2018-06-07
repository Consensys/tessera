package com.github.nexus.node;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class PartyInfoPostDelegate {

    private Client client;

    public PartyInfoPostDelegate(Client client) {
        this.client = client;
    }

    public byte[] doPost(String url, byte[] data) {
        Response response = client.target(url).path("/partyinfo").request()
            .post(Entity.entity(data,MediaType.APPLICATION_OCTET_STREAM_TYPE));

        return response.readEntity(byte[].class);
    }

}
