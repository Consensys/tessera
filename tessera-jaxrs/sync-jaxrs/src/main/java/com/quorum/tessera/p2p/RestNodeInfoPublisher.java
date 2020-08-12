package com.quorum.tessera.p2p;

import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Objects;

public class RestNodeInfoPublisher implements NodeInfoPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestNodeInfoPublisher.class);

    private final Client client;

    private final PartyInfoParser partyInfoParser;

    public RestNodeInfoPublisher(final Client client, final PartyInfoParser parser) {
        this.client = Objects.requireNonNull(client);
        this.partyInfoParser = Objects.requireNonNull(parser);
    }

    @Override
    public boolean publishNodeInfo(final String targetUrl, final NodeInfo existingNodeInfo) {

        final PartyInfo legacyInfoStructure = PartyInfo.from(existingNodeInfo);
        final byte[] encodedPartyInfo = this.partyInfoParser.to(legacyInfoStructure);

        LOGGER.debug("Sending PartyInfo to peer {}", targetUrl);

        try (Response response =
                 client.target(targetUrl)
                     .path("/partyinfo")
                     .request()
                     .post(Entity.entity(encodedPartyInfo, MediaType.APPLICATION_OCTET_STREAM_TYPE))) {

            final int returnStatusCode = response.getStatus();
            if (Response.Status.OK.getStatusCode() != returnStatusCode
                && Response.Status.CREATED.getStatusCode() != returnStatusCode) {
                LOGGER.warn("PartyInfo returned status code for peer {} was {}", targetUrl, returnStatusCode);
                return false;
            }

            LOGGER.debug("Successful PartyInfo call to {}", targetUrl);
            return Objects.nonNull(response.readEntity(byte[].class));
        }


    }
}
