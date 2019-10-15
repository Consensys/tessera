package com.quorum.tessera.p2p;

import com.quorum.tessera.partyinfo.PartyInfoValidatorCallback;
import com.quorum.tessera.partyinfo.model.Recipient;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class RestPartyInfoValidatorCallback implements PartyInfoValidatorCallback {

    private final Client restClient;

    public RestPartyInfoValidatorCallback(Client restClient) {
        this.restClient = restClient;
    }

    @Override
    public String requestDecode(Recipient recipient, byte[] encodedPayloadData) {

        Response response
                = restClient
                        .target(recipient.getUrl())
                        .path("partyinfo")
                        .path("validate")
                        .request()
                        .post(Entity.entity(encodedPayloadData, MediaType.APPLICATION_OCTET_STREAM));

        return response.readEntity(String.class);
    }

}
