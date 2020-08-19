package com.quorum.tessera.p2p;

import com.quorum.tessera.discovery.NodeUri;
import com.quorum.tessera.partyinfo.P2pClient;
import com.quorum.tessera.partyinfo.node.Party;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.net.URI;
import java.util.Objects;
import java.util.stream.Stream;

public class RestP2pClient implements P2pClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestP2pClient.class);

    private final Client client;

    public RestP2pClient(final Client client) {
        this.client = Objects.requireNonNull(client);
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

    @Override
    public Stream<Party> getParties(URI uri) {

        try(Response response = client.target(uri)
            .path("partyinfo").request().get()) {

            LOGGER.debug("Get parties from {}",uri);

            if(Status.OK.getStatusCode() != response.getStatus()) {
                throw new RuntimeException(response.getStatusInfo().getReasonPhrase());
            }

            JsonObject data = response.readEntity(JsonObject.class);

            LOGGER.debug("Party info response {} from {}",data,uri);

            return data.getJsonArray("peers").stream()
                .map(JsonValue::asJsonObject)
                .map(o -> o.getString("url"))
                .map(NodeUri::create)
                .map(NodeUri::asString)
                .map(Party::new);

        }
    }
}
