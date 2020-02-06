package com.quorum.tessera.p2p;

import com.quorum.tessera.partyinfo.P2pClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Objects;

public class RestP2pClient implements P2pClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestP2pClient.class);

    private final Client client;

    public RestP2pClient(final Client client) {
        this.client = Objects.requireNonNull(client);
    }

    @Override
    public byte[] push(final String targetUrl, final byte[] data) {
        LOGGER.debug("Sending Transaction via /push to peer {}", targetUrl);

        try (Response response =
                 client.target(targetUrl)
                     .path("/push")
                     .request()
                     .post(Entity.entity(data, MediaType.APPLICATION_OCTET_STREAM_TYPE))) {

            final int returnStatusCode = response.getStatus();
            if (Response.Status.OK.getStatusCode() != returnStatusCode
                && Response.Status.CREATED.getStatusCode() != returnStatusCode) {
                LOGGER.warn("Push returned status code for peer {} was {}", targetUrl, returnStatusCode);
                return null;
            }

            LOGGER.debug("Successful Push call to {}", targetUrl);
            return response.readEntity(byte[].class);
        }
    }

    @Override
    public boolean sendPartyInfo(final String targetUrl, final byte[] data) {
        LOGGER.debug("Sending PartyInfo to peer {}", targetUrl);

        try (Response response =
                 client.target(targetUrl)
                     .path("/partyinfo")
                     .request()
                     .post(Entity.entity(data, MediaType.APPLICATION_OCTET_STREAM_TYPE))) {

            final int returnStatusCode = response.getStatus();
            if (Status.OK.getStatusCode() != returnStatusCode
                && Status.CREATED.getStatusCode() != returnStatusCode) {
                LOGGER.warn("PartyInfo returned status code for peer {} was {}", targetUrl, returnStatusCode);
                return false;
            }

            LOGGER.debug("Successful PartyInfo call to {}", targetUrl);
            return Objects.nonNull(response.readEntity(byte[].class));
        }
    }
}
