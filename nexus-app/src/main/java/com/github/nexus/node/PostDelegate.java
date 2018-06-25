package com.github.nexus.node;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class PostDelegate {

    private Client client;

    public PostDelegate(final Client client){
        this.client = client;
    }

    /**
     * POST request to specified url, where the request is a binary data array.
     * Returns the response as a byte array, or null if the request failed
     */
    public byte[] doPost(final String url, final String path, final byte[] data) {

        final Response response = client
            .target(url)
            .path(path)
            .request()
            .post(Entity.entity(data, MediaType.APPLICATION_OCTET_STREAM_TYPE));

        if (Response.Status.OK.getStatusCode() != response.getStatus()) {
            return null;
        }

        return response.readEntity(byte[].class);
    }

}
