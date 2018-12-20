package com.quorum.tessera.client;

import com.quorum.tessera.api.model.ResendRequest;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Objects;

/**
 * Makes HTTP Post calls on behalf of the application
 * Abstracts away the need to know how the input data is serialised and what client is used
 */
public class PostDelegate {

    private final Client client;

    public PostDelegate(final Client client) {
        this.client = Objects.requireNonNull(client);
    }

    /**
     * Makes a post request for a given set of parameters
     * Sends the entity as an OCTET_STREAM and returns the response only if a 2xx response received.
     *
     * @param url  the target URL to call
     * @param path the path of the URL to call
     * @param data the data that should be sent as an OCTET STREAM
     * @return the response as a series of bytes
     */
    public byte[] doPost(final URI url, final String path, final byte[] data) {

        final Response response = client
            .target(url)
            .path(path)
            .request()
            .post(Entity.entity(data, MediaType.APPLICATION_OCTET_STREAM_TYPE));

        if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
            return null;
        }

        return response.readEntity(byte[].class);
    }

    /**
     * Makes a request to resend transactions to the provided node.
     * The request objects is passed in with all the settings that are needed for this request preset.
     * <p>
     * Does not handle any exceptions that arise.
     *
     * @param url           The target url to make the request to
     * @param resendRequest The request body object that gets serialised
     * @return whether the request was completed successfully or not
     */
    public boolean makeResendRequest(final URI url, final ResendRequest resendRequest) {

        final Response response = client
            .target(url)
            .path("/resend")
            .request()
            .post(Entity.entity(resendRequest, MediaType.APPLICATION_JSON));

        return response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL;
    }

}
