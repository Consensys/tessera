package com.quorum.tessera.test.rest;

import com.quorum.tessera.api.model.SendRequest;
import com.quorum.tessera.test.ClientFacade;
import com.quorum.tessera.test.Party;
import com.quorum.tessera.test.RestPartyHelper;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.quorum.tessera.test.PartyHelper;

public class RestClientFacade implements ClientFacade {

    private Client  client = RestUtils.buildClient();

    private PartyHelper partyHelper = new RestPartyHelper();
    
    @Override
    public Response send(Party sender, byte[] transactionData, Party... recipients) {
        String[] recipientArray = Stream.of(recipients)
            .map(Party::getPublicKey)
            .collect(Collectors.toList())
            .toArray(new String[recipients.length]);

        final SendRequest sendRequest = new SendRequest();
        
        sendRequest.setFrom(sender.getPublicKey());
        sendRequest.setTo(recipientArray);
        sendRequest.setPayload(transactionData);

        return this.client.target(sender.getUri())
            .path("send")
            .request()
            .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));
        
 

    }

    @Override
    public Response send(URI senderUri, byte[] transactionData, Party... recipients) {
        String[] recipientArray = Stream.of(recipients)
            .map(Party::getPublicKey)
            .collect(Collectors.toList())
            .toArray(new String[recipients.length]);

        final SendRequest sendRequest = new SendRequest();
        sendRequest.setTo(recipientArray);
        sendRequest.setPayload(transactionData);

        return this.client.target(senderUri)
            .path("send")
            .request()
            .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

    }
    
    public boolean allNodesAreUp() {
        return partyHelper.getParties().allMatch(this::isUp);
    }

    @Override
    public boolean isUp(Party party) {
        return client.target(party.getUri())
            .path("upcheck")
            .request()
            .get().getStatus() == 200;
    }

    @Override
    public Response find(Party party, String transactionId) {
        
        String encodedId = urlEncode(transactionId);

        return Optional.of(party)
            .map(p -> client.target(p.getUri()))
            .map(target -> target.path("transaction"))
            .map(target -> target.path(encodedId))
            .map(target -> target.request().get()).get();

    }
    
       String urlEncode(String data) {
        try {
            return URLEncoder.encode(data, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

}
